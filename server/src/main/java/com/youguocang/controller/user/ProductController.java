package com.youguocang.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youguocang.common.ApiResult;
import com.youguocang.common.PageResult;
import com.youguocang.entity.Category;
import com.youguocang.entity.Product;
import com.youguocang.mapper.CategoryMapper;
import com.youguocang.mapper.ProductMapper;
import com.youguocang.security.UserContext;
import com.youguocang.service.OperationLogService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端商品浏览与检索。
 *
 * <p>对应文档「商品浏览与检索模块」：分类浏览、关键词/价格/产地筛选。
 */
@RestController
@RequestMapping("/api/user/products")
public class ProductController {

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final OperationLogService operationLogService;

    public ProductController(ProductMapper productMapper, CategoryMapper categoryMapper,
                             OperationLogService operationLogService) {
        this.productMapper = productMapper;
        this.categoryMapper = categoryMapper;
        this.operationLogService = operationLogService;
    }

    /**
     * 商品列表。
     *
     * @param sort sales（默认销量） / price_asc / price_desc / new
     */
    @GetMapping
    public ApiResult<PageResult<Product>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String origin,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "12") long size,
            @RequestParam(defaultValue = "sales") String sort) {

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .eq(Product::getDeleted, 0)
                // 列表页不返回长文本详情，减少带宽
                .select(Product.class, info -> !"detail".equals(info.getColumn()));

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Product::getName, keyword);
        }
        if (categoryId != null) {
            List<Long> categoryIds = collectCategoryIds(categoryId);
            wrapper.in(Product::getCategoryId, categoryIds);
        }
        if (minPrice != null) {
            wrapper.ge(Product::getPrice, minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(Product::getPrice, maxPrice);
        }
        if (StringUtils.hasText(origin)) {
            wrapper.like(Product::getOriginPlace, origin);
        }

        switch (sort) {
            case "price_asc" -> wrapper.orderByAsc(Product::getPrice);
            case "price_desc" -> wrapper.orderByDesc(Product::getPrice);
            case "new" -> wrapper.orderByDesc(Product::getCreatedAt);
            default -> wrapper.orderByDesc(Product::getSalesCount);
        }

        Page<Product> result = productMapper.selectPage(new Page<>(page, size), wrapper);
        return ApiResult.ok(PageResult.of(result.getRecords(), result.getTotal(), page, size));
    }

    /** 商品详情，同时把详情页访问记入操作日志（便于运营分析）。 */
    @GetMapping("/{id}")
    public ApiResult<Product> detail(@PathVariable Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getDeleted() != null && product.getDeleted() == 1) {
            return ApiResult.fail(com.youguocang.common.BizCode.NOT_FOUND, "商品不存在");
        }
        operationLogService.record("PRODUCT_VIEW", "商品#" + id,
                "用户 " + UserContext.requireUserId() + " 查看商品");
        return ApiResult.ok(product);
    }

    /** 取分类及其子分类 ID。 */
    private List<Long> collectCategoryIds(Long categoryId) {
        List<Long> ids = new ArrayList<>();
        ids.add(categoryId);
        List<Category> children = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().eq(Category::getParentId, categoryId));
        ids.addAll(children.stream().map(Category::getId).collect(Collectors.toList()));
        return ids;
    }
}
