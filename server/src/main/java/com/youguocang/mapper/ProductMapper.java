package com.youguocang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youguocang.entity.Product;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /** 全站热销榜：上架商品按累计销量倒序。 */
    @Select("SELECT * FROM t_product WHERE deleted = 0 AND status = 1 "
            + "ORDER BY sales_count DESC LIMIT #{limit}")
    List<Product> selectHot(@Param("limit") int limit);

    /** 指定分类（含其子分类）的热销榜。 */
    @Select("SELECT * FROM t_product WHERE deleted = 0 AND status = 1 "
            + "AND category_id IN (SELECT id FROM t_category WHERE parent_id = #{categoryId} OR id = #{categoryId}) "
            + "ORDER BY sales_count DESC LIMIT #{limit}")
    List<Product> selectHotByCategory(@Param("categoryId") Long categoryId, @Param("limit") int limit);

    /** 批量取上架商品，用于把推荐结果补齐为可展示的商品信息。 */
    @Select("<script>SELECT * FROM t_product WHERE deleted = 0 AND status = 1 AND id IN "
            + "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
            + "</script>")
    List<Product> selectOnlineByIds(@Param("ids") List<Long> ids);
}
