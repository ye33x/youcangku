package com.youguocang.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youguocang.common.ApiResult;
import com.youguocang.common.BizCode;
import com.youguocang.common.BizException;
import com.youguocang.dto.CartItemVO;
import com.youguocang.entity.CartItem;
import com.youguocang.entity.Product;
import com.youguocang.mapper.CartItemMapper;
import com.youguocang.mapper.ProductMapper;
import com.youguocang.security.UserContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端购物车。
 *
 * <p>对应文档「购物车模块」：修改数量、勾选/取消勾选、移除、结算前预览。
 * 同一商品重复加入时按唯一键合并数量，而不是新增一行。
 */
@RestController
@RequestMapping("/api/user/cart")
public class CartController {

    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;

    public CartController(CartItemMapper cartItemMapper, ProductMapper productMapper) {
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
    }

    /** 加入购物车（已存在则累加数量）。 */
    @PostMapping
    public ApiResult<Long> add(@RequestBody AddRequest request) {
        Long userId = UserContext.requireUserId();
        int quantity = request.quantity() == null || request.quantity() <= 0 ? 1 : request.quantity();

        Product product = productMapper.selectById(request.productId());
        if (product == null || product.getStatus() == null || product.getStatus() != 1) {
            throw new BizException(BizCode.NOT_FOUND, "商品已下架");
        }
        if (product.getStock() != null && product.getStock() < quantity) {
            throw new BizException(BizCode.CONFLICT, "库存不足");
        }

        CartItem existing = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getProductId, request.productId()));
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            cartItemMapper.updateById(existing);
            return ApiResult.ok(existing.getId());
        }

        CartItem item = new CartItem();
        item.setUserId(userId);
        item.setProductId(request.productId());
        item.setQuantity(quantity);
        item.setChecked(1);
        cartItemMapper.insert(item);
        return ApiResult.ok(item.getId());
    }

    /** 购物车列表（附带商品信息与小计）。 */
    @GetMapping
    public ApiResult<List<CartItemVO>> list() {
        Long userId = UserContext.requireUserId();
        List<CartItem> items = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .orderByDesc(CartItem::getCreatedAt));
        if (items.isEmpty()) {
            return ApiResult.ok(List.of());
        }
        List<Long> productIds = items.stream().map(CartItem::getProductId).distinct().toList();
        Map<Long, Product> productById = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));

        List<CartItemVO> result = items.stream()
                .filter(item -> productById.containsKey(item.getProductId()))
                .map(item -> {
                    Product product = productById.get(item.getProductId());
                    BigDecimal subtotal = product.getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    return new CartItemVO(item.getId(), product.getId(), product.getName(),
                            product.getMainImage(), product.getSpec(), product.getPrice(),
                            item.getQuantity(), item.getChecked(), product.getStock(), subtotal);
                })
                .toList();
        return ApiResult.ok(result);
    }

    /** 修改数量或勾选状态。 */
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody UpdateRequest request) {
        CartItem item = requireOwned(id);
        if (request.quantity() != null) {
            if (request.quantity() <= 0) {
                throw new BizException(BizCode.BAD_REQUEST, "数量必须大于 0");
            }
            item.setQuantity(request.quantity());
        }
        if (request.checked() != null) {
            item.setChecked(request.checked() == 0 ? 0 : 1);
        }
        cartItemMapper.updateById(item);
        return ApiResult.ok();
    }

    /** 移除商品。 */
    @DeleteMapping("/{id}")
    public ApiResult<Void> remove(@PathVariable Long id) {
        requireOwned(id);
        cartItemMapper.deleteById(id);
        return ApiResult.ok();
    }

    /** 结算预览：只汇总已勾选项。 */
    @GetMapping("/checkout-preview")
    public ApiResult<BigDecimal> checkoutPreview() {
        List<CartItem> checked = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, UserContext.requireUserId())
                .eq(CartItem::getChecked, 1));
        if (checked.isEmpty()) {
            return ApiResult.ok(BigDecimal.ZERO);
        }
        List<Long> ids = checked.stream().map(CartItem::getProductId).distinct().toList();
        Map<Long, Product> productById = productMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : checked) {
            Product product = productById.get(item.getProductId());
            if (product != null) {
                total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }
        return ApiResult.ok(total);
    }

    private CartItem requireOwned(Long id) {
        CartItem item = cartItemMapper.selectById(id);
        if (item == null || !item.getUserId().equals(UserContext.requireUserId())) {
            throw new BizException(BizCode.NOT_FOUND, "购物车条目不存在");
        }
        return item;
    }

    public record AddRequest(Long productId, Integer quantity) {
    }

    public record UpdateRequest(Integer quantity, Integer checked) {
    }
}
