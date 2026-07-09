package com.fons.cloud.admin.domain.model;

import com.fons.cloud.admin.api.enums.GovernanceSnapshotType;
import com.fons.cloud.admin.domain.entity.AdminGovernanceSnapshot;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 治理快照领域模型，负责生成发布前、发布后、回滚来源和漂移当前值快照实体。
 */
@Getter
public class GovernanceSnapshot {

    private final Long resourceId;
    private final Long changeId;
    private final Long releaseId;
    private final GovernanceSnapshotType snapshotType;
    private final String content;
    private final String contentHash;

    private GovernanceSnapshot(Long resourceId, Long changeId, Long releaseId, GovernanceSnapshotType snapshotType,
                               String content, String contentHash) {
        this.resourceId = resourceId;
        this.changeId = changeId;
        this.releaseId = releaseId;
        this.snapshotType = snapshotType;
        this.content = content;
        this.contentHash = StringUtils.defaultIfBlank(contentHash, hash(content));
    }

    /**
     * 创建治理快照。
     *
     * @param resourceId   治理资源 ID
     * @param changeId     关联变更 ID
     * @param releaseId    关联发布记录 ID，草稿来源快照可为空
     * @param snapshotType 快照类型
     * @param content      快照正文
     * @param contentHash  快照正文 hash
     * @return 治理快照领域对象
     */
    public static GovernanceSnapshot create(Long resourceId, Long changeId, Long releaseId,
                                            GovernanceSnapshotType snapshotType, String content, String contentHash) {
        return new GovernanceSnapshot(resourceId, changeId, releaseId, snapshotType, content, contentHash);
    }

    /**
     * 转换为可落库的快照实体。contentSummary 会脱敏并截断，避免列表和审计场景暴露敏感正文。
     *
     * @return 快照持久化实体
     */
    public AdminGovernanceSnapshot toEntity() {
        AdminGovernanceSnapshot entity = new AdminGovernanceSnapshot();
        entity.setResourceId(resourceId);
        entity.setChangeId(changeId);
        entity.setReleaseId(releaseId);
        entity.setSnapshotType(snapshotType.name());
        entity.setContent(content);
        entity.setContentHash(contentHash);
        entity.setContentSummary(summary(content, contentHash));
        return entity;
    }

    /**
     * 快照摘要只用于列表和审计场景，不承载完整配置正文、IP 列表、路由明细或密钥值。
     */
    private String summary(String content, String contentHash) {
        if (StringUtils.isBlank(content)) {
            return "配置内容为空";
        }
        return "配置内容已脱敏，长度=" + content.length() + "，hash=" + contentHash;
    }

    private static String hash(String content) {
        if (content == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(content.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", ex);
        }
    }
}
