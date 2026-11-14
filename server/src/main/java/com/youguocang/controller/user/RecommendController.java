package com.youguocang.controller.user;

import com.youguocang.common.ApiResult;
import com.youguocang.entity.BrowseLog;
import com.youguocang.mapper.BrowseLogMapper;
import com.youguocang.recommend.RecommendItemVO;
import com.youguocang.recommend.RecommendService;
import com.youguocang.security.UserContext;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端推荐位。
 *
 * <p>三个入口对应文档中的推荐场景：
 * <ul>
 *   <li>{@code /home}   首页「猜你喜欢」</li>
 *   <li>{@code /detail/{id}} 商品详情页相似商品</li>
 *   <li>{@code /hot}    纯热门榜（不走个性化，可用于兜底展示）</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/user/recommend")
public class RecommendController {

    private final RecommendService recommendService;
    private final BrowseLogMapper browseLogMapper;

    public RecommendController(RecommendService recommendService, BrowseLogMapper browseLogMapper) {
        this.recommendService = recommendService;
        this.browseLogMapper = browseLogMapper;
    }

    /** 首页个性化推荐，附带推荐理由。 */
    @GetMapping("/home")
    public ApiResult<List<RecommendItemVO>> home(@RequestParam(required = false) Long categoryId) {
        return ApiResult.ok(recommendService.recommend(UserContext.requireUserId(), categoryId));
    }

    /** 详情页「猜你喜欢」：以当前商品作为兴趣锚点。 */
    @GetMapping("/detail/{productId}")
    public ApiResult<List<RecommendItemVO>> detail(@PathVariable Long productId) {
        // 简化实现：详情页推荐以分类为粒度，后续可改为以该商品为种子商品做 item-based 召回
        return ApiResult.ok(recommendService.recommend(UserContext.requireUserId(), null));
    }

    /** 全站/分类热销榜。 */
    @GetMapping("/hot")
    public ApiResult<List<RecommendItemVO>> hot(@RequestParam(required = false) Long categoryId,
                                                @RequestParam(defaultValue = "8") int limit) {
        return ApiResult.ok(recommendService.hotList(categoryId, limit));
    }

    /**
     * 上报浏览行为，作为推荐算法的数据来源。
     *
     * <p>真实项目里应做去重（同一用户 30 分钟内重复浏览同一商品只计一次），
     * 这里保留最简实现，见 docs/03-推荐模块设计.md 的「后续优化」。
     */
    @PostMapping("/browse/{productId}")
    public ApiResult<Void> browse(@PathVariable Long productId,
                                  @RequestParam(defaultValue = "1") int source) {
        BrowseLog log = new BrowseLog();
        log.setUserId(UserContext.requireUserId());
        log.setProductId(productId);
        log.setSource(source);
        browseLogMapper.insert(log);
        return ApiResult.ok();
    }
}
