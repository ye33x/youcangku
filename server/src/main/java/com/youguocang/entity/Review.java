package com.youguocang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 商品评价（t_review）。提交后为「待审核」，审核通过才对前台可见。
 */
@Data
@TableName("t_review")
public class Review {

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_REJECTED = 2;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderItemId;
    private Long orderId;
    private Long userId;
    private Long productId;
    private Long merchantId;
    private Integer score;
    private String content;
    /** 逗号分隔的图片地址 */
    private String images;
    private Integer status;
    private Long auditAdminId;
    private String auditRemark;
    private LocalDateTime auditedAt;
    private LocalDateTime createdAt;
    private Integer deleted;
}
