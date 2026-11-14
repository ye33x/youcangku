package com.youguocang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youguocang.entity.ProductCooccur;
import com.youguocang.recommend.WeightedItem;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductCooccurMapper extends BaseMapper<ProductCooccur> {

    /** 查询若干物品的共现记录，用于实时推荐打分。 */
    @Select("<script>SELECT * FROM t_product_cooccur WHERE product_id IN "
            + "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
            + "</script>")
    List<ProductCooccur> selectByProductIds(@Param("ids") List<Long> ids);

    /** 每个商品的共现总次数（Σ 该商品所有共现对），用于余弦相似度归一化。 */
    @Select("SELECT product_id AS productId, SUM(co_count) AS weight FROM t_product_cooccur "
            + "GROUP BY product_id")
    List<WeightedItem> selectItemTotals();

    /** 离线任务重建前清空旧数据。注意：TRUNCATE 无结果集，必须用 @Update 而非 @Select。 */
    @Update("TRUNCATE TABLE t_product_cooccur")
    void truncateAll();
}
