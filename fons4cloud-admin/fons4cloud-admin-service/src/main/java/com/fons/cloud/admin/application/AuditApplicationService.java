package com.fons.cloud.admin.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fons.cloud.admin.api.request.AuditQueryRequest;
import com.fons.cloud.admin.api.response.GovernanceAuditResponse;
import com.fons.cloud.admin.domain.entity.AdminGovernanceAudit;
import com.fons.cloud.admin.domain.mapper.AdminGovernanceAuditMapper;
import com.fons.cloud.admin.domain.model.GovernanceAudit;
import com.fons.cloud.common.result.R;
import com.fons.cloud.admin.interfaces.rest.api.model.PageResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * admin 审计应用服务，提供审计写入、脱敏和查询能力。
 */
@Service
@RequiredArgsConstructor
public class AuditApplicationService {

    private final AdminGovernanceAuditMapper adminGovernanceAuditMapper;

    /**
     * 写入一条已脱敏的治理审计记录。
     *
     * @param audit 审计领域模型
     * @return 是否写入成功
     */
    public R<Boolean> record(GovernanceAudit audit) {
        AdminGovernanceAudit entity = audit.toEntity();
        return R.ok(adminGovernanceAuditMapper.insert(entity) > 0);
    }

    /**
     * 按条件查询治理审计记录。
     *
     * @param request 查询条件
     * @return 审计响应列表
     */
    public R<List<GovernanceAuditResponse>> query(AuditQueryRequest request) {
        LambdaQueryWrapper<AdminGovernanceAudit> wrapper = queryWrapper(request);
        wrapper.last("LIMIT 100");
        return R.ok(adminGovernanceAuditMapper.selectList(wrapper).stream().map(this::toResponse).toList());
    }

    /** 强制上限的审计分页查询。 */
    public R<PageResponse<GovernanceAuditResponse>> queryPage(AuditQueryRequest request, int offset, int limit) {
        int safeLimit = Math.min(100, Math.max(1, limit));
        int safeOffset = Math.max(0, offset);
        long current = safeOffset / safeLimit + 1L;
        Page<AdminGovernanceAudit> page = adminGovernanceAuditMapper.selectPage(new Page<>(current, safeLimit),
                queryWrapper(request));
        return R.ok(new PageResponse<>(page.getRecords().stream().map(this::toResponse).toList(), page.getTotal(),
                safeOffset, safeLimit));
    }

    private LambdaQueryWrapper<AdminGovernanceAudit> queryWrapper(AuditQueryRequest request) {
        LambdaQueryWrapper<AdminGovernanceAudit> wrapper = new LambdaQueryWrapper<>();
        if (request != null) {
            if (request.getDomain() != null) {
                wrapper.eq(AdminGovernanceAudit::getDomain, request.getDomain().getCode());
            }
            if (StringUtils.isNotBlank(request.getOperation())) {
                wrapper.eq(AdminGovernanceAudit::getOperation, request.getOperation());
            }
            if (StringUtils.isNotBlank(request.getOperatorId())) {
                wrapper.eq(AdminGovernanceAudit::getOperatorId, request.getOperatorId());
            }
            if (request.getResult() != null) {
                wrapper.eq(AdminGovernanceAudit::getResult, request.getResult().name());
            }
            if (request.getStartTime() != null) {
                wrapper.ge(AdminGovernanceAudit::getOperatedAt, request.getStartTime());
            }
            if (request.getEndTime() != null) {
                wrapper.le(AdminGovernanceAudit::getOperatedAt, request.getEndTime());
            }
            if (StringUtils.isNumeric(request.getResourceKey())) {
                wrapper.eq(AdminGovernanceAudit::getResourceId, Long.valueOf(request.getResourceKey()));
            }
        }
        wrapper.orderByDesc(AdminGovernanceAudit::getOperatedAt);
        return wrapper;
    }

    /**
     * 查询单条审计记录详情。
     *
     * @param id 审计记录 ID
     * @return 审计响应
     */
    public R<GovernanceAuditResponse> getById(Long id) {
        AdminGovernanceAudit audit = adminGovernanceAuditMapper.selectById(id);
        return audit == null ? R.failed() : R.ok(toResponse(audit));
    }

    private GovernanceAuditResponse toResponse(AdminGovernanceAudit audit) {
        return GovernanceAuditResponse.builder()
                .id(audit.getId())
                .domain(audit.getDomain())
                .resourceId(audit.getResourceId())
                .changeId(audit.getChangeId())
                .operation(audit.getOperation())
                .operatorId(audit.getOperatorId())
                .operatorName(audit.getOperatorName())
                .requestId(audit.getRequestId())
                .clientIp(audit.getClientIp())
                .result(audit.getResult())
                .detailSummary(GovernanceAudit.mask(audit.getDetailSummary()))
                .errorCode(audit.getErrorCode())
                .errorMessage(GovernanceAudit.mask(audit.getErrorMessage()))
                .operatedAt(audit.getOperatedAt())
                .build();
    }
}
