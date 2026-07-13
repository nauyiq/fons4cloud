package com.fons.cloud.admin.domain.codec;

import com.fons.cloud.admin.api.enums.GovernanceDomain;
import com.fons.cloud.admin.api.constants.AdminResultCode;
import com.fons.cloud.common.base.exception.BizException;
import org.springframework.stereotype.Component;

import java.util.List;

/** 按治理域和资源类型选择唯一 Codec，避免 Controller 分支复制配置规则。 */
@Component
public class GovernanceConfigCodecRegistry {

    private final List<GovernanceConfigCodec> codecs;

    public GovernanceConfigCodecRegistry(List<GovernanceConfigCodec> codecs) {
        this.codecs = List.copyOf(codecs);
    }

    public GovernanceConfigCodec required(GovernanceDomain domain, String resourceType) {
        return codecs.stream()
                .filter(codec -> codec.domain() == domain && codec.resourceTypes().contains(resourceType))
                .findFirst()
                .orElseThrow(() -> new BizException(AdminResultCode.ADMIN_VALIDATION_FAILED));
    }
}
