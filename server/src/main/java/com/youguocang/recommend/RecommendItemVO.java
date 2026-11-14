package com.youguocang.recommend;

import java.math.BigDecimal;

/**
 * 推荐位响应项：商品信息 + 推荐依据 + 策略标识。
 *
 * <p>{@code reason} 直接用于前端展示「猜你喜欢」下方的小字说明，
 * 让推荐结果可解释，而不是黑盒列表。
 */
public record RecommendItemVO(
        Long productId,
        String name,
        BigDecimal price,
        BigDecimal originPrice,
        String originPlace,
        String spec,
        String mainImage,
        Integer salesCount,
        double score,
        String strategy,
        String strategyLabel,
        String reason) {
}
