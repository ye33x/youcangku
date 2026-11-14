package com.youguocang.dto;

import java.math.BigDecimal;

/**
 * 购物车条目视图：把购物车行与商品信息合并，前端可直接渲染。
 */
public record CartItemVO(
        Long id,
        Long productId,
        String productName,
        String mainImage,
        String spec,
        BigDecimal price,
        Integer quantity,
        Integer checked,
        Integer stock,
        BigDecimal subtotal) {
}
