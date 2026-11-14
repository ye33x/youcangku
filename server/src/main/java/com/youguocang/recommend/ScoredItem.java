package com.youguocang.recommend;

/**
 * 算法输出的候选推荐项（纯计算结果，不含商品详情）。
 *
 * @param productId      被推荐的商品
 * @param score          推荐得分
 * @param sourceProductId 贡献最大的来源商品，用于生成「因为你看过 X」这类理由；
 *                        热门榜补位时为 null
 */
public record ScoredItem(Long productId, double score, Long sourceProductId) {
}
