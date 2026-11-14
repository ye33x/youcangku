package com.youguocang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 商品共现统计（t_product_cooccur）。
 *
 * <p>同一用户对商品 A、B 都产生过行为，则 (A, B) 共现计数 +1。
 * 由离线任务 {@code CooccurrenceJob} 周期性全量重建，读多写少的场景下比实时计算便宜得多。
 */
@Data
@TableName("t_product_cooccur")
public class ProductCooccur {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;
    private Long relatedId;
    private Integer coCount;
    private LocalDateTime updatedAt;
}
