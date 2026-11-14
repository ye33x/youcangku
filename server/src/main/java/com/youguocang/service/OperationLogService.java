package com.youguocang.service;

import com.youguocang.common.BizCode;
import com.youguocang.common.BizException;
import com.youguocang.entity.OperationLog;
import com.youguocang.mapper.OperationLogMapper;
import com.youguocang.security.LoginUser;
import com.youguocang.security.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 操作日志写入。
 *
 * <p>只提供写入能力，查询走 {@code AdminLogController}，全项目没有任何删除日志的接口，
 * 保证「操作留痕、不可篡改」。
 */
@Service
public class OperationLogService {

    private static final Logger log = LoggerFactory.getLogger(OperationLogService.class);

    private final OperationLogMapper operationLogMapper;

    public OperationLogService(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    public void record(String action, String target, String detail) {
        try {
            LoginUser loginUser = UserContext.get();
            OperationLog entity = new OperationLog();
            if (loginUser != null) {
                entity.setOperatorType(loginUser.getRole().getCode() == 3
                        ? OperationLog.OPERATOR_ADMIN : OperationLog.OPERATOR_MERCHANT);
                entity.setOperatorId(loginUser.getId());
                entity.setOperatorName(loginUser.getName());
            } else {
                entity.setOperatorType(OperationLog.OPERATOR_ADMIN);
                entity.setOperatorId(0L);
                entity.setOperatorName("system");
            }
            entity.setAction(action);
            entity.setTarget(target);
            entity.setDetail(detail);
            operationLogMapper.insert(entity);
        } catch (RuntimeException ex) {
            // 日志失败不应影响主流程
            log.warn("操作日志写入失败: action={}, target={}", action, target, ex);
        }
    }

    /** 记录当前请求来源 IP。 */
    public void record(String action, String target, String detail, HttpServletRequest request) {
        record(action, target, detail + " | ip=" + resolveIp(request));
    }

    private String resolveIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /** 供管理端查询时复用。 */
    public void requireAdmin() {
        if (UserContext.require().getRole() != com.youguocang.common.RoleType.ADMIN) {
            throw new BizException(BizCode.FORBIDDEN);
        }
    }
}
