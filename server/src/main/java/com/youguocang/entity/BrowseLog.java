package com.youguocang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 浏览行为日志（t_browse_log），推荐算法的原始数据来源。
 */
@Data
@TableName("t_browse_log")
public class BrowseLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long productId;
    /** 1首页 2分类页 3搜索 4推荐位 */
    private Integer source;
    private LocalDateTime createdAt;
}
