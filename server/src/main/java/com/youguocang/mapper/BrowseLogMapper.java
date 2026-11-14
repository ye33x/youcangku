package com.youguocang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youguocang.entity.BrowseLog;
import com.youguocang.recommend.UserBehaviorRow;
import com.youguocang.recommend.WeightedItem;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BrowseLogMapper extends BaseMapper<BrowseLog> {

    /** 某用户对每个商品的行为次数，作为协同过滤的兴趣权重。 */
    @Select("SELECT product_id AS productId, COUNT(*) AS weight FROM t_browse_log "
            + "WHERE user_id = #{userId} GROUP BY product_id")
    List<WeightedItem> selectUserWeights(@Param("userId") Long userId);

    /** 全量（用户, 商品）行为聚合，供离线共现任务使用。 */
    @Select("SELECT user_id AS userId, product_id AS productId, COUNT(*) AS weight FROM t_browse_log "
            + "GROUP BY user_id, product_id")
    List<UserBehaviorRow> selectAllUserItemWeights();
}
