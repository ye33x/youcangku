package com.youguocang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 商家回复（t_review_reply），与评价同样需要审核。
 */
@Data
@TableName("t_review_reply")
public class ReviewReply {

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_REJECTED = 2;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reviewId;
    private Long merchantId;
    private String content;
    private Integer status;
    private LocalDateTime auditedAt;
    private LocalDateTime createdAt;
}
