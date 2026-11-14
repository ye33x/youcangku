package com.youguocang.recommend;

/**
 * 商品行为权重（商品 ID + 权重），由 Mapper 直接映射查询结果。
 */
public class WeightedItem {

    private Long productId;
    private Double weight;

    public WeightedItem() {
    }

    public WeightedItem(Long productId, Double weight) {
        this.productId = productId;
        this.weight = weight;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }
}
