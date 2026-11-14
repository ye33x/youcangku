package com.youguocang.recommend;

import com.youguocang.entity.ProductCooccur;
import com.youguocang.mapper.BrowseLogMapper;
import com.youguocang.mapper.ProductCooccurMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 离线共现任务：把用户行为展开成「商品对」共现计数，重建 {@code t_product_cooccur}。
 *
 * <p>为什么离线算：在线请求如果每次都扫全量行为表，复杂度是 O(用户数 × 物品数²)，
 * 而共现矩阵变化很慢，每天凌晨全量重建一次足够，在线只做一次主键范围内的查询。
 *
 * <p>重建策略：先全量 TRUNCATE 再批量插入。演示规模（万级行为）够用；
 * 真实环境建议改为「影子表 + 原子 rename」，避免重建窗口内推荐为空。
 */
@Component
public class CooccurrenceJob {

    private static final Logger log = LoggerFactory.getLogger(CooccurrenceJob.class);

    /** 单个用户行为商品过多时截断，避免 O(n²) 爆炸（正常用户不会超过 50 个）。 */
    private static final int MAX_ITEMS_PER_USER = 50;

    private final BrowseLogMapper browseLogMapper;
    private final ProductCooccurMapper cooccurMapper;

    public CooccurrenceJob(BrowseLogMapper browseLogMapper, ProductCooccurMapper cooccurMapper) {
        this.browseLogMapper = browseLogMapper;
        this.cooccurMapper = cooccurMapper;
    }

    /** 每天 03:00 重建，可通过配置覆盖 cron。 */
    @Scheduled(cron = "${youguocang.recommend.cooccur-cron:0 0 3 * * ?}")
    public void scheduledRebuild() {
        try {
            rebuild();
        } catch (Exception ex) {
            log.error("共现任务执行失败", ex);
        }
    }

    public void rebuild() {
        long start = System.currentTimeMillis();
        List<UserBehaviorRow> rows = browseLogMapper.selectAllUserItemWeights();

        Map<Long, Set<Long>> itemsByUser = new HashMap<>();
        for (UserBehaviorRow row : rows) {
            if (row.getUserId() == null || row.getProductId() == null) {
                continue;
            }
            Set<Long> items = itemsByUser.computeIfAbsent(row.getUserId(), key -> new LinkedHashSet<>());
            if (items.size() < MAX_ITEMS_PER_USER) {
                items.add(row.getProductId());
            }
        }

        Map<String, Integer> pairCounts = new HashMap<>();
        for (Set<Long> items : itemsByUser.values()) {
            List<Long> list = new ArrayList<>(items);
            for (int i = 0; i < list.size(); i++) {
                for (int j = 0; j < list.size(); j++) {
                    if (i == j) {
                        continue;
                    }
                    Long a = list.get(i);
                    Long b = list.get(j);
                    pairCounts.merge(a + ":" + b, 1, Integer::sum);
                }
            }
        }

        cooccurMapper.truncateAll();
        int inserted = 0;
        for (Map.Entry<String, Integer> entry : pairCounts.entrySet()) {
            String[] parts = entry.getKey().split(":");
            ProductCooccur record = new ProductCooccur();
            record.setProductId(Long.valueOf(parts[0]));
            record.setRelatedId(Long.valueOf(parts[1]));
            record.setCoCount(entry.getValue());
            cooccurMapper.insert(record);
            inserted++;
        }
        log.info("共现矩阵重建完成: 用户数={}, 商品对数={}, 耗时={}ms",
                itemsByUser.size(), inserted, System.currentTimeMillis() - start);
    }
}
