package com.youguocang.recommend;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 基于物品的协同过滤（简化版，靠共现统计实现）。
 *
 * <p>不引入机器学习框架，只用一张 {@code t_product_cooccur} 表：
 * <pre>
 *   sim(a, b) = co(a, b) / sqrt(N_a * N_b)
 * </pre>
 * 其中 {@code N_x} 为物品 x 在共现表中所有共现次数之和，相当于对二值共现向量做余弦相似度。
 *
 * <p>打分：对用户已有行为的每个物品 i，把相似度按行为权重累加到候选商品上：
 * <pre>
 *   score(u, j) = Σ_i  weight(u, i) * sim(i, j)
 * </pre>
 * 同时记录贡献最大的来源物品，用于生成可解释的推荐理由。
 *
 * <p>本类不依赖数据库，便于单元测试。
 */
public class ItemCfRecommender {

    /**
     * @param userItems    用户对商品的兴趣权重（商品 ID → 权重）
     * @param cooccurrence 商品共现矩阵：商品 ID → (关联商品 ID → 共现次数)
     * @param itemTotals   每个商品的共现总次数：商品 ID → Σ co(该商品, *)，用于余弦归一化
     * @param exclude      需要排除的商品（用户已浏览/已购买，不再推荐）
     * @param topN         返回条数
     */
    public List<ScoredItem> recommend(Map<Long, Double> userItems,
                                      Map<Long, Map<Long, Integer>> cooccurrence,
                                      Map<Long, Integer> itemTotals,
                                      Set<Long> exclude,
                                      int topN) {
        if (userItems == null || userItems.isEmpty() || cooccurrence == null || cooccurrence.isEmpty()
                || itemTotals == null || itemTotals.isEmpty() || topN <= 0) {
            return List.of();
        }
        Set<Long> excluded = exclude == null ? Set.of() : exclude;

        Map<Long, Double> scores = new HashMap<>();
        Map<Long, Double> bestContribution = new HashMap<>();
        Map<Long, Long> bestSource = new HashMap<>();

        for (Map.Entry<Long, Double> entry : userItems.entrySet()) {
            Long sourceId = entry.getKey();
            Double userWeight = entry.getValue();
            if (sourceId == null || userWeight == null || userWeight <= 0) {
                continue;
            }
            Map<Long, Integer> row = cooccurrence.get(sourceId);
            if (row == null || row.isEmpty()) {
                continue;
            }
            int sourceTotal = itemTotals.getOrDefault(sourceId, 0);
            if (sourceTotal <= 0) {
                continue;
            }

            for (Map.Entry<Long, Integer> cell : row.entrySet()) {
                Long candidateId = cell.getKey();
                Integer coCount = cell.getValue();
                if (candidateId == null || candidateId.equals(sourceId) || excluded.contains(candidateId)
                        || coCount == null || coCount <= 0) {
                    continue;
                }
                // 归一化分母来自独立的聚合查询，不能只靠已加载的共现行推算：
                // 共现表按双向存储，用户只看了 A 时，候选商品 B 的那一行并没有被加载进来。
                int candidateTotal = itemTotals.getOrDefault(candidateId, 0);
                if (candidateTotal <= 0) {
                    continue;
                }
                double similarity = coCount / Math.sqrt((double) sourceTotal * candidateTotal);
                double contribution = similarity * userWeight;
                if (contribution <= 0) {
                    continue;
                }
                scores.merge(candidateId, contribution, Double::sum);
                if (contribution > bestContribution.getOrDefault(candidateId, 0d)) {
                    bestContribution.put(candidateId, contribution);
                    bestSource.put(candidateId, sourceId);
                }
            }
        }

        List<ScoredItem> result = new ArrayList<>(scores.size());
        for (Map.Entry<Long, Double> entry : scores.entrySet()) {
            result.add(new ScoredItem(entry.getKey(), entry.getValue(), bestSource.get(entry.getKey())));
        }
        result.sort(Comparator.comparingDouble(ScoredItem::score).reversed());
        return result.size() > topN ? result.subList(0, topN) : result;
    }
}
