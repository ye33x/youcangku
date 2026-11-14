package com.youguocang.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youguocang.common.ApiResult;
import com.youguocang.common.BizCode;
import com.youguocang.common.BizException;
import com.youguocang.common.PageResult;
import com.youguocang.entity.Review;
import com.youguocang.entity.ReviewReply;
import com.youguocang.mapper.ReviewMapper;
import com.youguocang.mapper.ReviewReplyMapper;
import com.youguocang.security.UserContext;
import com.youguocang.service.OperationLogService;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端内容审核。
 *
 * <p>对应文档「评价管理模块」：用户评价审核、商家回复审核。
 * 两级审核都通过后，前台才展示评价与回复。
 */
@RestController
@RequestMapping("/api/admin/reviews")
public class AdminReviewController {

    private final ReviewMapper reviewMapper;
    private final ReviewReplyMapper reviewReplyMapper;
    private final OperationLogService operationLogService;

    public AdminReviewController(ReviewMapper reviewMapper, ReviewReplyMapper reviewReplyMapper,
                                 OperationLogService operationLogService) {
        this.reviewMapper = reviewMapper;
        this.reviewReplyMapper = reviewReplyMapper;
        this.operationLogService = operationLogService;
    }

    /** 评价列表，status 不传则全部。 */
    @GetMapping
    public ApiResult<PageResult<Review>> list(@RequestParam(required = false) Integer status,
                                              @RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "10") long size) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<Review>()
                .orderByAsc(Review::getStatus)
                .orderByDesc(Review::getCreatedAt);
        if (status != null) {
            wrapper.eq(Review::getStatus, status);
        }
        Page<Review> result = reviewMapper.selectPage(new Page<>(page, size), wrapper);
        return ApiResult.ok(PageResult.of(result.getRecords(), result.getTotal(), page, size));
    }

    /** 审核用户评价。 */
    @PutMapping("/{id}/audit")
    public ApiResult<Void> audit(@PathVariable Long id, @RequestBody AuditRequest request) {
        Review review = reviewMapper.selectById(id);
        if (review == null) {
            throw new BizException(BizCode.NOT_FOUND, "评价不存在");
        }
        if (review.getStatus() != null && review.getStatus() != Review.STATUS_PENDING) {
            throw new BizException(BizCode.CONFLICT, "该评价已审核");
        }
        review.setStatus(request.approve() ? Review.STATUS_APPROVED : Review.STATUS_REJECTED);
        review.setAuditAdminId(UserContext.requireUserId());
        review.setAuditRemark(request.remark());
        review.setAuditedAt(LocalDateTime.now());
        reviewMapper.updateById(review);

        operationLogService.record(request.approve() ? "REVIEW_APPROVE" : "REVIEW_REJECT",
                "评价#" + id, request.remark());
        return ApiResult.ok();
    }

    /** 审核商家回复。 */
    @PutMapping("/replies/{id}/audit")
    public ApiResult<Void> auditReply(@PathVariable Long id, @RequestBody AuditRequest request) {
        ReviewReply reply = reviewReplyMapper.selectById(id);
        if (reply == null) {
            throw new BizException(BizCode.NOT_FOUND, "回复不存在");
        }
        if (reply.getStatus() != null && reply.getStatus() != ReviewReply.STATUS_PENDING) {
            throw new BizException(BizCode.CONFLICT, "该回复已审核");
        }
        reply.setStatus(request.approve() ? ReviewReply.STATUS_APPROVED : ReviewReply.STATUS_REJECTED);
        reply.setAuditedAt(LocalDateTime.now());
        reviewReplyMapper.updateById(reply);

        operationLogService.record(request.approve() ? "REPLY_APPROVE" : "REPLY_REJECT",
                "回复#" + id, request.remark());
        return ApiResult.ok();
    }

    /** 待审核数量，用于管理端角标。 */
    @GetMapping("/pending-count")
    public ApiResult<Long> pendingCount() {
        Long reviews = reviewMapper.selectCount(new LambdaQueryWrapper<Review>()
                .eq(Review::getStatus, Review.STATUS_PENDING));
        Long replies = reviewReplyMapper.selectCount(new LambdaQueryWrapper<ReviewReply>()
                .eq(ReviewReply::getStatus, ReviewReply.STATUS_PENDING));
        return ApiResult.ok((reviews == null ? 0 : reviews) + (replies == null ? 0 : replies));
    }

    public record AuditRequest(boolean approve, String remark) {
    }
}
