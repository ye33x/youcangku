package com.youguocang.recommend;

import com.youguocang.entity.Category;
import com.youguocang.entity.Product;
import com.youguocang.mapper.BrowseLogMapper;
import com.youguocang.mapper.CategoryMapper;
import com.youguocang.mapper.ProductCooccurMapper;
import com.youguocang.mapper.ProductMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 推荐编排：决定走哪条策略、补位、生成推荐理由。
 *
 * <p>策略选择：
 * <ol>
 *   <li>用户无任何行为 → {@link RecommendStrategy#COLD_START}，全站/分类热销 + 基础理由</li>
 *   <li>行为数低于阈值 → {@link RecommendStrategy#HYBRID}，协同过滤结果不足时用热门补位</li>
 *   <li>行为数达到阈值 → {@link RecommendStrategy#ITEM_CF}</li>
 * </ol>
 */
@Service
public class RecommendService {

    private static final Logger log = LoggerFactory.getLogger(RecommendService.class);

    private final ProductMapper productMapper;
    private final BrowseLogMapper browseLogMapper;
    private final ProductCooccurMapper cooccurMapper;
    private final CategoryMapper categoryMapper;
    private final RecommendProperties properties;

    private final ItemCfRecommender itemCfRecommender = new ItemCfRecommender();
    private final ColdStartRecommender coldStartRecommender = new ColdStartRecommender();

    public RecommendService(ProductMapper productMapper,
                            BrowseLogMapper browseLogMapper,
                            ProductCooccurMapper cooccurMapper,
                            CategoryMapper categoryMapper,
                            RecommendProperties properties) {
        this.productMapper = productMapper;
        this.browseLogMapper = browseLogMapper;
        this.cooccurMapper = cooccurMapper;
        this.categoryMapper = categoryMapper;
        this.properties = properties;
    }

    /**
     * 生成推荐列表。
     *
     * @param userId     当前用户，允许为 null（未登录，直接冷启动）
     * @param categoryId 限定分类，允许为 null（全站）
     */
    public List<RecommendItemVO> recommend(Long userId, Long categoryId) {
        int topN = properties.getTopN();
        String categoryName = resolveCategoryName(categoryId);

        Map<Long, Double> userWeights = loadUserWeights(userId);
        List<Product> hotProducts = loadHotProducts(categoryId, Math.max(topN * 2, 8));

        RecommendStrategy strategy;
        List<ScoredItem> scored;
        Map<Long, Integer> hotRank = new LinkedHashMap<>();
        // 热销名次在两种策略下都会用到（混合补位时同样需要给出「第 N 名」）
        for (int i = 0; i < hotProducts.size(); i++) {
            hotRank.put(hotProducts.get(i).getId(), i + 1);
        }

        if (userWeights.isEmpty()) {
            // 冷启动：完全没有行为数据
            strategy = RecommendStrategy.COLD_START;
            scored = coldStartRecommender.recommend(hotProducts, topN);
        } else {
            Map<Long, Map<Long, Integer>> cooccurrence = loadCooccurrence(userWeights.keySet());
            Map<Long, Integer> itemTotals = loadItemTotals();
            scored = new ArrayList<>(itemCfRecommender.recommend(
                    userWeights, cooccurrence, itemTotals, userWeights.keySet(), topN));

            boolean sparse = userWeights.size() < properties.getColdStartThreshold();
            // 协同过滤命中不足时用热销补位：列表总是填满，但策略标记为 HYBRID，
            // 让调用方与推荐日志能区分「算法结果」和「热门兜底」。
            if (sparse || scored.size() < topN) {
                strategy = RecommendStrategy.HYBRID;
                scored = fillWithHot(scored, hotProducts, topN);
            } else {
                strategy = RecommendStrategy.ITEM_CF;
            }
        }

        return toViewModels(scored, strategy, categoryName, hotProducts, hotRank);
    }

    /** 首页/详情页的热销榜（不走个性化）。 */
    public List<RecommendItemVO> hotList(Long categoryId, int limit) {
        String categoryName = resolveCategoryName(categoryId);
        List<Product> hotProducts = loadHotProducts(categoryId, limit);
        List<ScoredItem> scored = coldStartRecommender.recommend(hotProducts, limit);
        Map<Long, Integer> hotRank = new LinkedHashMap<>();
        for (int i = 0; i < hotProducts.size(); i++) {
            hotRank.put(hotProducts.get(i).getId(), i + 1);
        }
        return toViewModels(scored, RecommendStrategy.COLD_START, categoryName, hotProducts, hotRank);
    }

    // ------------------------------------------------------------------
    // 数据加载
    // ------------------------------------------------------------------

    private Map<Long, Double> loadUserWeights(Long userId) {
        if (userId == null) {
            return Map.of();
        }
        List<WeightedItem> rows = browseLogMapper.selectUserWeights(userId);
        if (rows == null || rows.isEmpty()) {
            return Map.of();
        }
        Map<Long, Double> weights = new HashMap<>(rows.size());
        for (WeightedItem row : rows) {
            if (row.getProductId() != null && row.getWeight() != null && row.getWeight() > 0) {
                // 多次浏览按对数压缩，避免单个商品刷屏式浏览主导推荐
                weights.put(row.getProductId(), 1.0 + Math.log(row.getWeight()));
            }
        }
        return weights;
    }

    private Map<Long, Map<Long, Integer>> loadCooccurrence(Set<Long> userProductIds) {
        List<Long> ids = userProductIds.stream()
                .limit(properties.getCandidateLimit())
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<com.youguocang.entity.ProductCooccur> rows = cooccurMapper.selectByProductIds(ids);
        Map<Long, Map<Long, Integer>> matrix = new HashMap<>();
        for (com.youguocang.entity.ProductCooccur row : rows) {
            matrix.computeIfAbsent(row.getProductId(), key -> new HashMap<>())
                    .put(row.getRelatedId(), row.getCoCount());
        }
        return matrix;
    }

    /**
     * 加载每个商品的共现总次数。
     *
     * <p>必须单独聚合，不能从已加载的共现行里求和：共现表按双向存储，
     * 用户只看过 A 时，候选商品 B 的那一行并不会被 {@link #loadCooccurrence} 取出来，
     * 用它求和会得到 0，进而把候选全部过滤掉。
     */
    private Map<Long, Integer> loadItemTotals() {
        List<WeightedItem> rows = cooccurMapper.selectItemTotals();
        if (rows == null || rows.isEmpty()) {
            return Map.of();
        }
        Map<Long, Integer> totals = new HashMap<>(rows.size());
        for (WeightedItem row : rows) {
            if (row.getProductId() != null && row.getWeight() != null) {
                totals.put(row.getProductId(), row.getWeight().intValue());
            }
        }
        return totals;
    }

    private List<Product> loadHotProducts(Long categoryId, int limit) {
        List<Product> products = categoryId == null
                ? productMapper.selectHot(limit)
                : productMapper.selectHotByCategory(categoryId, limit);
        return products == null ? List.of() : products;
    }

    private String resolveCategoryName(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        Category category = categoryMapper.selectById(categoryId);
        return category == null ? null : category.getName();
    }

    // ------------------------------------------------------------------
    // 组装
    // ------------------------------------------------------------------

    private List<ScoredItem> fillWithHot(List<ScoredItem> scored, List<Product> hotProducts, int topN) {
        List<ScoredItem> merged = new ArrayList<>(scored);
        Set<Long> existing = scored.stream().map(ScoredItem::productId).collect(Collectors.toSet());
        for (Product product : hotProducts) {
            if (merged.size() >= topN) {
                break;
            }
            if (existing.contains(product.getId())) {
                continue;
            }
            merged.add(new ScoredItem(product.getId(), 0d, null));
            existing.add(product.getId());
        }
        return merged;
    }

    private List<RecommendItemVO> toViewModels(List<ScoredItem> scored,
                                               RecommendStrategy strategy,
                                               String categoryName,
                                               List<Product> hotProducts,
                                               Map<Long, Integer> hotRank) {
        if (scored.isEmpty()) {
            return List.of();
        }
        List<Long> ids = new ArrayList<>();
        for (ScoredItem item : scored) {
            ids.add(item.productId());
            if (item.sourceProductId() != null) {
                ids.add(item.sourceProductId());
            }
        }
        Map<Long, Product> productById = productMapper.selectBatchIds(ids.stream().distinct().toList())
                .stream()
                .collect(Collectors.toMap(Product::getId, product -> product, (a, b) -> a));

        List<RecommendItemVO> result = new ArrayList<>(scored.size());
        for (ScoredItem item : scored) {
            Product product = productById.get(item.productId());
            if (product == null || product.getStatus() == null || product.getStatus() != 1
                    || (product.getDeleted() != null && product.getDeleted() == 1)) {
                continue;
            }
            String reason = buildReason(item, product, productById, categoryName, hotRank);
            result.add(new RecommendItemVO(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    product.getOriginPrice(),
                    product.getOriginPlace(),
                    product.getSpec(),
                    product.getMainImage(),
                    product.getSalesCount(),
                    item.score(),
                    strategy.name(),
                    strategy.getLabel(),
                    reason));
        }
        log.debug("推荐完成: strategy={}, size={}", strategy, result.size());
        return result;
    }

    private String buildReason(ScoredItem item,
                               Product product,
                               Map<Long, Product> productById,
                               String categoryName,
                               Map<Long, Integer> hotRank) {
        if (item.sourceProductId() != null) {
            Product source = productById.get(item.sourceProductId());
            String sourceName = source == null ? "浏览过的商品" : "「" + shorten(source.getName()) + "」";
            return "因为你浏览过" + sourceName + "，同类买家也常一起看";
        }
        Integer rank = hotRank.get(item.productId());
        String rankText = rank == null ? "热销" : "热销第 " + rank + " 名";
        int sales = product.getSalesCount() == null ? 0 : product.getSalesCount();
        if (categoryName != null) {
            return categoryName + "分类" + rankText + "，已售 " + sales + " 件";
        }
        return "全站" + rankText + "，已售 " + sales + " 件";
    }

    private static String shorten(String name) {
        if (name == null) {
            return "";
        }
        return name.length() <= 12 ? name : name.substring(0, 12) + "…";
    }
}
