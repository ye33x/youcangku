package com.youguocang.recommend;

import com.youguocang.entity.Product;
import java.util.List;

/**
 * 冷启动降级：当用户没有足够行为数据时，用全站或分类热销榜兜底。
 *
 * <p>之所以单独成类而不塞进 ItemCf：这两条路径的数据来源和排序口径完全不同，
 * 分开后既方便各自测试，也方便日后把「热门」换成「按点击率加权」而不影响协同过滤。
 */
public class ColdStartRecommender {

    /**
     * @param hotProducts 已按销量倒序排列的商品（通常是热销榜 Top N）
     * @param topN        返回条数
     */
    public List<ScoredItem> recommend(List<Product> hotProducts, int topN) {
        if (hotProducts == null || hotProducts.isEmpty() || topN <= 0) {
            return List.of();
        }
        return hotProducts.stream()
                .limit(topN)
                .map(product -> new ScoredItem(
                        product.getId(),
                        product.getSalesCount() == null ? 0d : product.getSalesCount().doubleValue(),
                        null))
                .toList();
    }
}
