package com.youguocang.recommend;

/**
 * 推荐策略，会写入推荐日志与接口响应，便于对比效果。
 */
public enum RecommendStrategy {

    /** 冷启动：用户没有任何行为数据，只能依据全站/分类热门兜底。 */
    COLD_START("新用户热门推荐"),

    /** 行为数据充足：纯基于物品的协同过滤。 */
    ITEM_CF("基于物品的协同过滤"),

    /** 行为数据偏少：协同过滤结果 + 热门补足。 */
    HYBRID("协同过滤结合热门补足");

    private final String label;

    RecommendStrategy(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
