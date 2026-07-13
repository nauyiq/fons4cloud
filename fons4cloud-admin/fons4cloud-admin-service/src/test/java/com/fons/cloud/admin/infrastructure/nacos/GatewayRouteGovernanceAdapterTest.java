package com.fons.cloud.admin.infrastructure.nacos;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.fons.cloud.admin.api.response.GovernanceValidateResult;
import com.fons.cloud.admin.domain.adapter.GovernanceTargetAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * 网关路由 Nacos 治理适配器测试。
 */
@ExtendWith(MockitoExtension.class)
class GatewayRouteGovernanceAdapterTest {

    @Mock
    private NacosConfigManager nacosConfigManager;
    @Mock
    private ConfigService configService;

    private GatewayRouteGovernanceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new GatewayRouteGovernanceAdapter(nacosConfigManager, "gateway-routing.json", "DEFAULT_GROUP");
    }

    @Test
    void springContextShouldCreateAdapterWithNacosPropertiesConstructor() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                    "admin.gateway.route.data-id=gateway-routing.json",
                    "admin.gateway.route.group=DEFAULT_GROUP");
            context.registerBean(NacosConfigProperties.class, NacosConfigProperties::new);
            context.register(GatewayRouteGovernanceAdapter.class);

            context.refresh();

            assertThat(context.getBean(GatewayRouteGovernanceAdapter.class).domain()).isEqualTo(adapter.domain());
        }
    }

    @Test
    void validateShouldPassForRouteDefinitionJsonArray() {
        GovernanceValidateResult result = adapter.validate(new GovernanceTargetAdapter.TargetConfig(resourceRef(),
                """
                        [{"id":"demo","uri":"lb://demo-service","predicates":[{"name":"Path","args":{"pattern":"/demo/**"}}]}]
                        """, null));

        assertThat(result.getPassed()).isTrue();
        assertThat(result.getNormalizedContentHash()).isNotBlank();
    }

    @Test
    void validateShouldReturnFieldErrorsForInvalidRoute() {
        GovernanceValidateResult result = adapter.validate(new GovernanceTargetAdapter.TargetConfig(resourceRef(),
                "[{\"id\":\"\",\"uri\":\"\",\"predicates\":[]}]", null));

        assertThat(result.getPassed()).isFalse();
        assertThat(result.getErrors()).extracting("code")
                .contains("ROUTE_ID_EMPTY", "ROUTE_URI_EMPTY", "ROUTE_PREDICATES_EMPTY");
    }

    @Test
    void validateShouldReturnFieldErrorsForMissingRouteFields() {
        GovernanceValidateResult result = adapter.validate(new GovernanceTargetAdapter.TargetConfig(resourceRef(),
                "[{}]", null));

        assertThat(result.getPassed()).isFalse();
        assertThat(result.getErrors()).extracting("code")
                .contains("ROUTE_ID_EMPTY", "ROUTE_URI_EMPTY", "ROUTE_PREDICATES_EMPTY");
    }

    @Test
    void loadCurrentShouldReadNacosConfigAndReturnHash() throws Exception {
        when(nacosConfigManager.getConfigService()).thenReturn(configService);
        when(configService.getConfig("gateway-routing.json", "DEFAULT_GROUP", 5000L))
                .thenReturn("[{\"id\":\"demo\",\"uri\":\"lb://demo-service\",\"predicates\":[{\"name\":\"Path\"}]}]");

        GovernanceTargetAdapter.CurrentConfig currentConfig = adapter.loadCurrent(resourceRef());

        assertThat(currentConfig.content()).contains("\"id\":\"demo\"");
        assertThat(currentConfig.contentHash()).isNotBlank();
        assertThat(currentConfig.targetRef()).isEqualTo("gateway-routing.json");
    }

    @Test
    void loadCurrentShouldUseConfiguredDataIdInsteadOfRouteResourceKey() throws Exception {
        when(nacosConfigManager.getConfigService()).thenReturn(configService);
        when(configService.getConfig("gateway-routing.json", "DEFAULT_GROUP", 5000L))
                .thenReturn("[{\"id\":\"orders\",\"uri\":\"lb://orders-service\",\"predicates\":[{\"name\":\"Path\"}]}]");

        GovernanceTargetAdapter.CurrentConfig currentConfig = adapter.loadCurrent(new GovernanceTargetAdapter.ResourceRef(
                adapter.domain(), "ROUTE", "orders", "orders"));

        assertThat(currentConfig.targetRef()).isEqualTo("gateway-routing.json");
    }

    @Test
    void publishShouldWriteNormalizedConfigToNacos() throws Exception {
        when(nacosConfigManager.getConfigService()).thenReturn(configService);
        when(configService.getConfig("gateway-routing.json", "DEFAULT_GROUP", 5000L))
                .thenReturn("[{\"id\":\"old\",\"uri\":\"lb://old-service\",\"predicates\":[{\"name\":\"Path\"}]}]");
        when(configService.publishConfig(org.mockito.ArgumentMatchers.eq("gateway-routing.json"),
                org.mockito.ArgumentMatchers.eq("DEFAULT_GROUP"), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(true);

        GovernanceTargetAdapter.AdapterPublishResult result = adapter.publish(new GovernanceTargetAdapter.TargetConfig(
                resourceRef(),
                "[{\"id\":\"new\",\"uri\":\"lb://new-service\",\"predicates\":[{\"name\":\"Path\"}]}]",
                "hash-new"), new GovernanceTargetAdapter.PublishContext("REL-001", "operator", "publish", "hash-old"));

        assertThat(result.success()).isTrue();
        assertThat(result.beforeContent()).contains("\"id\":\"old\"");
        assertThat(result.afterContent()).contains("\"id\":\"new\"");
        assertThat(result.effectiveHint()).contains("Nacos");
    }

    private GovernanceTargetAdapter.ResourceRef resourceRef() {
        return new GovernanceTargetAdapter.ResourceRef(adapter.domain(), "ROUTE", "gateway-routing.json",
                "gateway-routing.json");
    }
}
