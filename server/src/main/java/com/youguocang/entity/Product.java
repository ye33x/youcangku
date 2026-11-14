package com.youguocang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 商品（t_product）。
 */
@Data
@TableName("t_product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;
    private Long shopId;
    private Long categoryId;
    private String name;
    private BigDecimal price;
    private BigDecimal originPrice;
    private String originPlace;
    private String spec;
    private Integer stock;
    /** 冗余销量，供热门榜排序 */
    private Integer salesCount;
    private String mainImage;
    private String detail;
    /** 0下架 1上架 */
    private Integer status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
