package com.youguocang.recommend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * 协同过滤核心逻辑的单元测试（不依赖数据库）。
 *
 * <p>运行：{@code mvn -q -Dtest=ItemCfRecommenderTest test}
 */
class ItemCfRecommenderTest {

    private final ItemCfRecommender recommender = new ItemCfRecommender();

    /** 与用户已看商品共现的商品应被推荐，并记录来源商品用于生成理由。 */
    @Test
    void shouldRecommendCooccurringItemAndTrackSource() {
        Map<Long, Map<Long, Integer>> cooccurrence = Map.of(
                1L, Map.of(2L, 3));
        Map<Long, Integer> totals = Map.of(1L, 3, 2L, 3);

        List<ScoredItem> result = recommender.recommend(Map.of(1L, 1.0), cooccurrence, totals, Set.of(), 5);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).productId());
        assertEquals(1L, result.get(0).sourceProductId());
        assertTrue(result.get(0).score() > 0, "得分应为正数");
    }

    /**
     * 回归测试：候选商品的共现行没有被加载，也必须能算出得分。
     *
     * <p>共现表双向存储。用户只看过商品 1，查询只带回 product_id=1 的行；
     * 若用「已加载行求和」当作候选的归一化分母，会得到 0 从而把候选 2 过滤掉。
     */
    @Test
    void shouldScoreCandidateWhoseOwnRowWasNotLoaded() {
        Map<Long, Map<Long, Integer>> cooccurrence = Map.of(
                1L, Map.of(2L, 3));
        Map<Long, Integer> totals = Map.of(1L, 3, 2L, 3);

        List<ScoredItem> result = recommender.recommend(Map.of(1L, 1.0), cooccurrence, totals, Set.of(), 5);

        assertFalse(result.isEmpty(), "候选商品缺失自身共现行时仍应被推荐");
        assertEquals(2L, result.get(0).productId());
    }

    /** 已交互过的商品必须被排除。 */
    @Test
    void shouldExcludeInteractedItems() {
        Map<Long, Map<Long, Integer>> cooccurrence = Map.of(
                1L, Map.of(2L, 3));
        Map<Long, Integer> totals = Map.of(1L, 3, 2L, 3);

        List<ScoredItem> result = recommender.recommend(Map.of(1L, 1.0), cooccurrence, totals, Set.of(2L), 5);

        assertTrue(result.isEmpty());
    }

    /** 无行为数据时不应抛异常，直接返回空列表，由上层走冷启动。 */
    @Test
    void shouldReturnEmptyForColdUser() {
        assertTrue(recommender.recommend(Map.of(), Map.of(), Map.of(), Set.of(), 5).isEmpty());
    }

    /** 结果按得分倒序。 */
    @Test
    void shouldSortByScoreDescending() {
        Map<Long, Map<Long, Integer>> cooccurrence = Map.of(
                1L, Map.of(2L, 5, 3L, 1));
        Map<Long, Integer> totals = Map.of(1L, 6, 2L, 5, 3L, 1);

        List<ScoredItem> result = recommender.recommend(Map.of(1L, 1.0), cooccurrence, totals, Set.of(), 5);

        assertFalse(result.isEmpty());
        for (int i = 1; i < result.size(); i++) {
            assertTrue(result.get(i - 1).score() >= result.get(i).score());
        }
    }

    /** 冷启动只做截断，不改排序（排序由 SQL 的销量倒序保证）。 */
    @Test
    void coldStartShouldTruncateHotList() {
        com.youguocang.entity.Product first = new com.youguocang.entity.Product();
        first.setId(10L);
        first.setSalesCount(100);
        com.youguocang.entity.Product second = new com.youguocang.entity.Product();
        second.setId(20L);
        second.setSalesCount(50);

        List<ScoredItem> result = new ColdStartRecommender()
                .recommend(List.of(first, second), 1);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).productId());
    }
}
