package com.fons.cloud.admin.infrastructure.limiter;

import com.fons.cloud.admin.api.response.GovernanceValidateResult;
import com.fons.cloud.admin.domain.adapter.GovernanceTargetAdapter;
import com.fons.cloud.limiter.api.ManualWhiteIpService;
import com.fons.cloud.limiter.core.BlockDTO;
import com.fons.cloud.limiter.core.ManualBlockedIpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 流量治理 IP 黑白名单适配器测试。
 */
@ExtendWith(MockitoExtension.class)
class TrafficIpGovernanceAdapterTest {

    @Mock
    private ManualWhiteIpService manualWhiteIpService;
    @Mock
    private ManualBlockedIpService manualBlockedIpService;

    private TrafficIpGovernanceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TrafficIpGovernanceAdapter(manualWhiteIpService, manualBlockedIpService);
    }

    @Test
    void validateShouldPassForWhiteAndManualBlockedIps() {
        GovernanceValidateResult result = adapter.validate(new GovernanceTargetAdapter.TargetConfig(resourceRef(),
                """
                        {"whiteIps":["10.0.0.1"],"manualBlockedIps":[{"ip":"10.0.0.2","blockSeconds":60}]}
                        """, null));

        assertThat(result.getPassed()).isTrue();
        assertThat(result.getNormalizedContentHash()).isNotBlank();
    }

    @Test
    void validateShouldReturnFieldErrorsForInvalidIpAndBlockSeconds() {
        GovernanceValidateResult result = adapter.validate(new GovernanceTargetAdapter.TargetConfig(resourceRef(),
                """
                        {"whiteIps":["999.1.1.1"],"manualBlockedIps":[{"ip":"10.0.0.2","blockSeconds":0},{"ip":"10.0.0.2","blockSeconds":30}]}
                        """, null));

        assertThat(result.getPassed()).isFalse();
        assertThat(result.getErrors()).extracting("code")
                .contains("TRAFFIC_IP_INVALID", "TRAFFIC_BLOCK_SECONDS_INVALID", "TRAFFIC_IP_DUPLICATED");
    }

    @Test
    void loadCurrentShouldReadLimiterServicesAndReturnHash() {
        when(manualWhiteIpService.getAllWhiteIp()).thenReturn(Set.of("10.0.0.1"));
        when(manualBlockedIpService.getAllBlocked())
                .thenReturn(Map.of("10.0.0.2", new BlockDTO(60000L, System.currentTimeMillis())));

        GovernanceTargetAdapter.CurrentConfig currentConfig = adapter.loadCurrent(resourceRef());

        assertThat(currentConfig.content()).contains("10.0.0.1", "10.0.0.2", "\"blockSeconds\":60");
        assertThat(currentConfig.contentHash()).isNotBlank();
        assertThat(currentConfig.targetRef()).isEqualTo("limiter:manual-ip-list");
    }

    @Test
    void publishShouldApplyWhiteAndManualBlockedIpDiff() {
        when(manualWhiteIpService.getAllWhiteIp())
                .thenReturn(Set.of("10.0.0.1"))
                .thenReturn(Set.of("10.0.0.2"));
        when(manualBlockedIpService.getAllBlocked())
                .thenReturn(Map.of("10.0.0.3", new BlockDTO(30000L, System.currentTimeMillis())))
                .thenReturn(Map.of("10.0.0.4", new BlockDTO(60000L, System.currentTimeMillis())));

        GovernanceTargetAdapter.AdapterPublishResult result = adapter.publish(new GovernanceTargetAdapter.TargetConfig(
                resourceRef(),
                """
                        {"whiteIps":["10.0.0.2"],"manualBlockedIps":[{"ip":"10.0.0.4","blockSeconds":60}]}
                        """, null), new GovernanceTargetAdapter.PublishContext("REL-001", "operator", "publish", null));

        assertThat(result.success()).isTrue();
        assertThat(result.beforeContent()).contains("10.0.0.1", "10.0.0.3");
        assertThat(result.afterContent()).contains("10.0.0.2", "10.0.0.4");
        verify(manualWhiteIpService).removeWhiteIp("10.0.0.1");
        verify(manualWhiteIpService).addWhiteIp("10.0.0.2");
        verify(manualBlockedIpService).removeBlockIp("10.0.0.3");
        verify(manualBlockedIpService).addBlockIp("10.0.0.4", 60);
    }

    private GovernanceTargetAdapter.ResourceRef resourceRef() {
        return new GovernanceTargetAdapter.ResourceRef(adapter.domain(), "IP_LIST", "manual-ip-list",
                "limiter:manual-ip-list");
    }
}
