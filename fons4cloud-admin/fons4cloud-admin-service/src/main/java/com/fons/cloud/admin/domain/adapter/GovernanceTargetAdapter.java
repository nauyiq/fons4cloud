package com.fons.cloud.admin.domain.adapter;

import com.fons.cloud.admin.api.enums.GovernanceDomain;
import com.fons.cloud.admin.api.response.GovernanceValidateResult;

/**
 * 治理目标适配器端口。不同治理域通过该端口接入统一草稿、校验、发布和回滚流程。
 */
public interface GovernanceTargetAdapter {

    /**
     * 当前适配器负责的治理能力域。
     *
     * @return 治理能力域
     */
    GovernanceDomain domain();

    /**
     * 读取目标系统当前配置，用于创建草稿基线和发布前漂移检测。
     *
     * @param resourceRef 治理资源引用
     * @return 当前配置
     */
    CurrentConfig loadCurrent(ResourceRef resourceRef);

    /**
     * 校验期望配置是否可发布。
     *
     * @param targetConfig 期望配置
     * @return 校验结果
     */
    GovernanceValidateResult validate(TargetConfig targetConfig);

    /**
     * 发布期望配置到目标系统。
     *
     * @param targetConfig 期望配置
     * @param context      发布上下文
     * @return 发布结果
     */
    AdapterPublishResult publish(TargetConfig targetConfig, PublishContext context);

    /**
     * 判断资源是否支持完整回滚。
     *
     * @param resourceRef 治理资源引用
     * @return 是否支持回滚
     */
    boolean rollbackSupported(ResourceRef resourceRef);

    /**
     * 治理资源引用。resourceKey 由适配器按目标系统规则解释，targetRef 是 Nacos dataId、Redis key 或客户端 ID 等权威目标引用。
     */
    record ResourceRef(GovernanceDomain domain, String resourceType, String resourceKey, String targetRef) {
    }

    /**
     * 目标系统当前配置。
     */
    record CurrentConfig(String content, String contentHash, String targetRef) {
    }

    /**
     * 期望发布的目标配置。
     */
    record TargetConfig(ResourceRef resourceRef, String content, String contentHash) {
    }

    /**
     * 发布上下文，包含发布流水、操作人、发布原因和用户确认的基线 hash。
     */
    record PublishContext(String releaseNo, String operatorId, String reason, String expectedBaseHash) {
    }

    /**
     * 目标适配器发布结果。失败时必须返回错误码和脱敏错误摘要。
     */
    record AdapterPublishResult(boolean success, String beforeContent, String beforeHash, String afterContent,
                                String afterHash, String errorCode, String errorMessage, String effectiveHint) {
    }
}
