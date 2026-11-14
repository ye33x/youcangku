package com.youguocang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 订单主表（t_order）。
 *
 * <p>状态流转：
 * <pre>
 *   0 待付款 --支付--> 1 待发货 --发货--> 2 待收货 --确认收货--> 3 已完成
 *      |                 |                 |
 *      +--取消--> 4 已取消 +--售后--> 5 售后中 &lt;--+
 * </pre>
 */
@Data
@TableName("t_order")
public class Order {

    public static final int STATUS_UNPAID = 0;
    public static final int STATUS_UNSHIPPED = 1;
    public static final int STATUS_SHIPPED = 2;
    public static final int STATUS_FINISHED = 3;
    public static final int STATUS_CANCELLED = 4;
    public static final int STATUS_AFTER_SALE = 5;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;
    private Long userId;
    private Long merchantId;
    private Integer status;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private String receiver;
    private String receiverPhone;
    private String receiverAddr;
    private String remark;
    private LocalDateTime paidAt;
    private LocalDateTime shippedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
