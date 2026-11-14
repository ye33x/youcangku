package com.youguocang.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youguocang.common.ApiResult;
import com.youguocang.common.PageResult;
import com.youguocang.entity.OperationLog;
import com.youguocang.mapper.OperationLogMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端操作日志。
 *
 * <p>按文档要求「日志仅支持查看筛选，不提供删除功能」，
 * 因此本控制器<b>刻意不暴露任何 DELETE 接口</b>。
 */
@RestController
@RequestMapping("/api/admin/logs")
public class AdminLogController {

    private final OperationLogMapper operationLogMapper;

    public AdminLogController(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @GetMapping
    public ApiResult<PageResult<OperationLog>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Integer operatorType,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {

        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<OperationLog>()
                .orderByDesc(OperationLog::getCreatedAt)
                .ge(from != null, OperationLog::getCreatedAt, from == null ? null : from.atStartOfDay())
                .lt(to != null, OperationLog::getCreatedAt,
                        to == null ? null : to.plusDays(1).atStartOfDay())
                .eq(operatorType != null, OperationLog::getOperatorType, operatorType)
                .eq(operatorId != null, OperationLog::getOperatorId, operatorId)
                .eq(StringUtils.hasText(action), OperationLog::getAction, action);

        Page<OperationLog> result = operationLogMapper.selectPage(new Page<>(page, size), wrapper);
        return ApiResult.ok(PageResult.of(result.getRecords(), result.getTotal(), page, size));
    }

    /** 按操作类型统计，便于运营看板。 */
    @GetMapping("/action-stats")
    public ApiResult<java.util.List<java.util.Map<String, Object>>> actionStats(
            @RequestParam(defaultValue = "7") int days) {
        LocalDateTime since = LocalDate.now().minusDays(days).atStartOfDay();
        java.util.List<OperationLog> logs = operationLogMapper.selectList(
                new LambdaQueryWrapper<OperationLog>()
                        .ge(OperationLog::getCreatedAt, since)
                        .select(OperationLog::getAction));
        java.util.Map<String, Long> counter = logs.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        OperationLog::getAction, java.util.stream.Collectors.counting()));
        return ApiResult.ok(counter.entrySet().stream()
                .sorted(java.util.Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> java.util.Map.<String, Object>of("action", entry.getKey(), "count", entry.getValue()))
                .toList());
    }
}
