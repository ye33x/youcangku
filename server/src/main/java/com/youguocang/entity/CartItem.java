package com.youguocang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 购物车条目（t_cart_item）。
 *
 * <p>(user_id, product_id) 唯一：同一商品重复加入时合并数量而非新增行。
 */
@Data
@TableName("t_cart_item")
public class CartItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long productId;
    private Integer quantity;
    /** 0取消勾选 1勾选 */
    private Integer checked;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
