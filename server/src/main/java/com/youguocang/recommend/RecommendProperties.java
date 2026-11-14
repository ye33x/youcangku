package com.youguocang.recommend;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 推荐相关可调参数（application.yml 中 youguocang.recommend.*）。
 */
@Component
@ConfigurationProperties(prefix = "youguocang.recommend")
public class RecommendProperties {

    /** 每次返回的推荐条数 */
    private int topN = 8;

    /** 用户有效行为（去重商品数）低于该值即认为行为稀疏，走混合策略 */
    private int coldStartThreshold = 3;

    /** 参与共现查询的候选商品上限，防止用户行为过多时 SQL 过长 */
    private int candidateLimit = 200;

    public int getTopN() {
        return topN;
    }

    public void setTopN(int topN) {
        this.topN = topN;
    }

    public int getColdStartThreshold() {
        return coldStartThreshold;
    }

    public void setColdStartThreshold(int coldStartThreshold) {
        this.coldStartThreshold = coldStartThreshold;
    }

    public int getCandidateLimit() {
        return candidateLimit;
    }

    public void setCandidateLimit(int candidateLimit) {
        this.candidateLimit = candidateLimit;
    }
}
