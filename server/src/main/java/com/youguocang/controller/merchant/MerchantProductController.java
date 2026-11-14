package com.youguocang.controller.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youguocang.common.ApiResult;
import com.youguocang.common.BizCode;
import com.youguocang.common.BizException;
import com.youguocang.common.PageResult;
import com.youguocang.entity.Product;
import com.youguocang.entity.Shop;
import com.youguocang.mapper.ProductMapper;
import com.youguocang.mapper.ShopMapper;
import com.youguocang.security.UserContext;
import com.youguocang.service.OperationLogService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家端商品管理。
 *
 * <p>所有写操作都会校验「商品属于当前商家」，防止越权修改他人商品；
 * 上架/下架等关键动作写入操作日志。
 */
@RestController
@RequestMapping("/api/merchant/products")
public class MerchantProductController {

    private final ProductMapper productMapper;
    private final ShopMapper shopMapper;
    private final OperationLogService operationLogService;

    public MerchantProductController(ProductMapper productMapper, ShopMapper shopMapper,
                                     OperationLogService operationLogService) {
        this.productMapper = productMapper;
        this.shopMapper = shopMapper;
        this.operationLogService = operationLogService;
    }

    /** 我的商品列表。 */
    @GetMapping
    public ApiResult<PageResult<Product>> list(@RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) Integer status,
                                               @RequestParam(defaultValue = "1") long page,
                                               @RequestParam(defaultValue = "10") long size) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getMerchantId, UserContext.requireUserId())
                .orderByDesc(Product::getCreatedAt);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Product::getName, keyword);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        Page<Product> result = productMapper.selectPage(new Page<>(page, size), wrapper);
        return ApiResult.ok(PageResult.of(result.getRecords(), result.getTotal(), page, size));
    }

    /** 发布商品（默认下架，需商家手动上架）。 */
    @PostMapping
    public ApiResult<Long> create(@Valid @RequestBody ProductRequest request) {
        Long merchantId = UserContext.requireUserId();
        Shop shop = shopMapper.selectOne(new LambdaQueryWrapper<Shop>().eq(Shop::getMerchantId, merchantId));
        if (shop == null) {
            throw new BizException(BizCode.NOT_FOUND, "请先完善店铺信息");
        }

        Product product = new Product();
        product.setMerchantId(merchantId);
        product.setShopId(shop.getId());
        product.setCategoryId(request.categoryId());
        product.setName(request.name());
        product.setPrice(request.price());
        product.setOriginPrice(request.originPrice());
        product.setOriginPlace(request.originPlace());
        product.setSpec(request.spec());
        product.setStock(request.stock() == null ? 0 : request.stock());
        product.setSalesCount(0);
        product.setMainImage(request.mainImage());
        product.setDetail(request.detail());
        product.setStatus(0);
        productMapper.insert(product);

        operationLogService.record("PRODUCT_CREATE", "商品#" + product.getId(), "发布商品 " + request.name());
        return ApiResult.ok(product.getId());
    }

    /** 编辑商品：价格、库存、描述等。 */
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody ProductRequest request) {
        Product product = requireOwned(id);
        if (request.name() != null) {
            product.setName(request.name());
        }
        if (request.categoryId() != null) {
            product.setCategoryId(request.categoryId());
        }
        if (request.price() != null) {
            product.setPrice(request.price());
        }
        if (request.originPrice() != null) {
            product.setOriginPrice(request.originPrice());
        }
        if (request.originPlace() != null) {
            product.setOriginPlace(request.originPlace());
        }
        if (request.spec() != null) {
            product.setSpec(request.spec());
        }
        if (request.stock() != null) {
            product.setStock(request.stock());
        }
        if (request.mainImage() != null) {
            product.setMainImage(request.mainImage());
        }
        if (request.detail() != null) {
            product.setDetail(request.detail());
        }
        productMapper.updateById(product);

        operationLogService.record("PRODUCT_UPDATE", "商品#" + id, "编辑商品 " + product.getName());
        return ApiResult.ok();
    }

    /** 上架 / 下架。 */
    @PutMapping("/{id}/shelf")
    public ApiResult<Void> shelf(@PathVariable Long id, @RequestParam int status) {
        if (status != 0 && status != 1) {
            throw new BizException(BizCode.BAD_REQUEST, "status 只能是 0（下架）或 1（上架）");
        }
        Product product = requireOwned(id);
        if (status == 1 && (product.getStock() == null || product.getStock() <= 0)) {
            throw new BizException(BizCode.CONFLICT, "库存为 0，无法上架");
        }
        product.setStatus(status);
        productMapper.updateById(product);

        operationLogService.record(status == 1 ? "PRODUCT_ONLINE" : "PRODUCT_OFFLINE",
                "商品#" + id, product.getName() + (status == 1 ? " 上架" : " 下架"));
        return ApiResult.ok();
    }

    /** 删除商品（逻辑删除）。 */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        Product product = requireOwned(id);
        productMapper.deleteById(id);
        operationLogService.record("PRODUCT_DELETE", "商品#" + id, "删除商品 " + product.getName());
        return ApiResult.ok();
    }

    private Product requireOwned(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || !product.getMerchantId().equals(UserContext.requireUserId())) {
            throw new BizException(BizCode.FORBIDDEN, "无权操作该商品");
        }
        return product;
    }

    public record ProductRequest(
            @NotBlank String name,
            @NotNull Long categoryId,
            @NotNull BigDecimal price,
            BigDecimal originPrice,
            String originPlace,
            String spec,
            Integer stock,
            String mainImage,
            String detail) {
    }
}
