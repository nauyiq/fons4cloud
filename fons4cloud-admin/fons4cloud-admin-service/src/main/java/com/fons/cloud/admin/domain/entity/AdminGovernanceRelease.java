package com.fons.cloud.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fons.cloud.db.entity.CommonEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.Date;

/**
 * admin 治理发布记录实体。
 */
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("admin_governance_release")
public class AdminGovernanceRelease extends CommonEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 治理变更 ID。
     */
    private Long changeId;

    /**
     * 发布流水号，用于发布和回滚追踪。
     */
    private String releaseNo;

    /**
     * 发布类型：PUBLISH 发布，ROLLBACK 回滚。
     */
    private String releaseType;

    /**
     * 发布状态：RUNNING 执行中，SUCCESS 成功，FAILED 失败。
     */
    private String status;

    /**
     * 发布前目标摘要。
     */
    private String beforeHash;

    /**
     * 发布后目标摘要。
     */
    private String afterHash;

    /**
     * 发布操作人 ID。
     */
    private String operatorId;

    /**
     * 发布开始时间。
     */
    private Date startedAt;

    /**
     * 发布结束时间。
     */
    private Date finishedAt;

    /**
     * 发布失败时记录的错误码。
     */
    private String errorCode;

    /**
     * 脱敏错误摘要。
     */
    private String errorMessage;
}
