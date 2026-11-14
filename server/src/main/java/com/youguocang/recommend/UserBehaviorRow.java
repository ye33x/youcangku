package com.youguocang.recommend;

/**
 * 用户-商品行为聚合行，供离线共现任务使用。
 */
public class UserBehaviorRow {

    private Long userId;
    private Long productId;
    private Integer weight;

    public UserBehaviorRow() {
    }

    public UserBehaviorRow(Long userId, Long productId, Integer weight) {
        this.userId = userId;
        this.productId = productId;
        this.weight = weight;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }
}
