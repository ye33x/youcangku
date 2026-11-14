package com.youguocang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 操作日志（t_operation_log）。
 *
 * <p>只允许写入与查询，不提供删除能力，保证审计链路完整。
 */
@Data
@TableName("t_operation_log")
public class OperationLog {

    public static final int OPERATOR_ADMIN = 1;
    public static final int OPERATOR_MERCHANT = 2;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer operatorType;
    private Long operatorId;
    private String operatorName;
    /** 操作类型编码，如 PRODUCT_OFFLINE、REVIEW_AUDIT */
    private String action;
    private String target;
    private String detail;
    private String ip;
    private LocalDateTime createdAt;
}
