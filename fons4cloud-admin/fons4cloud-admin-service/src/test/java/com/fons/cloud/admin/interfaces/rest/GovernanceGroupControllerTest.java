package com.fons.cloud.admin.interfaces.rest;

import cn.hutool.core.codec.Base64;
import com.alibaba.fastjson2.JSON;
import com.fons.cloud.admin.api.enums.GovernanceDomain;
import com.fons.cloud.admin.api.request.ActuatorProbeRequest;
import com.fons.cloud.admin.api.request.GovernanceDraftCreateRequest;
import com.fons.cloud.admin.api.response.ActuatorProbeResult;
import com.fons.cloud.admin.api.response.GovernanceChangeResponse;
import com.fons.cloud.admin.api.response.ServiceInstanceResponse;
import com.fons.cloud.admin.application.GovernancePublishService;
import com.fons.cloud.admin.infrastructure.actuator.ActuatorReadAdapter;
import com.fons.cloud.admin.infrastructure.discovery.ServiceDiscoveryReadAdapter;
import com.fons.cloud.auth.api.support.DefaultAuthUser;
import com.fons.cloud.auth.common.AuthUserHeaderConstants;
import com.fons.cloud.common.result.R;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 治理分组 REST API 测试。
 */
@ExtendWith(MockitoExtension.class)
class GovernanceGroupControllerTest {

    @Mock
    private GovernancePublishService governancePublishService;
    @Mock
    private ServiceDiscoveryReadAdapter serviceDiscoveryReadAdapter;
    @Mock
    private ActuatorReadAdapter actuatorReadAdapter;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        com.fons.cloud.auth.utils.AuthUtils.removeUser();
        mockMvc = MockMvcBuilders.standaloneSetup(
                new GatewayGovernanceController(governancePublishService),
                new TrafficGovernanceController(governancePublishService),
                new AccessGovernanceController(governancePublishService),
                new ClientGovernanceController(governancePublishService),
                new ServiceGovernanceController(serviceDiscoveryReadAdapter),
                new ObservabilityController(actuatorReadAdapter)
        ).build();
    }

    @Test
    void serviceApisShouldReturnServicesAndInstances() throws Exception {
        when(serviceDiscoveryReadAdapter.listServices()).thenReturn(List.of("fons4cloud-auth"));
        when(serviceDiscoveryReadAdapter.listInstances("fons4cloud-auth")).thenReturn(List.of(ServiceInstanceResponse.builder()
                .serviceName("fons4cloud-auth")
                .host("127.0.0.1")
                .port(18080)
                .healthy(true)
                .build()));

        mockMvc.perform(get("/admin/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value("fons4cloud-auth"));
        mockMvc.perform(get("/admin/services/fons4cloud-auth/instances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].host").value("127.0.0.1"));
    }

    @Test
    void observabilityProbeShouldReturnActuatorResult() throws Exception {
        when(actuatorReadAdapter.probe(any(ActuatorProbeRequest.class))).thenReturn(ActuatorProbeResult.builder()
                .serviceName("fons4cloud-auth")
                .endpointPath("/actuator/health")
                .available(true)
                .status("UP")
                .build());

        mockMvc.perform(post("/admin/observability/actuator/probe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"serviceName\":\"fons4cloud-auth\",\"endpointPath\":\"/actuator/health\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Test
    void groupedDraftApisShouldOverrideGovernanceDomainAndResourceType() throws Exception {
        when(governancePublishService.createDraft(any(GovernanceDraftCreateRequest.class), eq("9")))
                .thenReturn(R.ok(GovernanceChangeResponse.builder().id(1L).changeNo("CHG-001").build()));

        performDraft("/admin/gateway/routes/drafts");
        performDraft("/admin/traffic/ip-lists/drafts");
        performDraft("/admin/access/resources/drafts");
        performDraft("/admin/clients/drafts");

        ArgumentCaptor<GovernanceDraftCreateRequest> captor = ArgumentCaptor.forClass(GovernanceDraftCreateRequest.class);
        verify(governancePublishService, org.mockito.Mockito.times(4)).createDraft(captor.capture(), eq("9"));
        assertThat(captor.getAllValues()).extracting(GovernanceDraftCreateRequest::getDomain)
                .containsExactly(GovernanceDomain.GATEWAY, GovernanceDomain.TRAFFIC,
                        GovernanceDomain.ACCESS, GovernanceDomain.CLIENTS);
        assertThat(captor.getAllValues()).extracting(GovernanceDraftCreateRequest::getResourceType)
                .containsExactly("ROUTE", "IP_LIST", "AUTH_RESOURCE", "OAUTH_CLIENT");
    }

    private void performDraft(String path) throws Exception {
        mockMvc.perform(post(path)
                        .header(AuthUserHeaderConstants.AUTH_USER, authUserHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"resourceKey":"demo","content":"{}"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private String authUserHeader() {
        DefaultAuthUser authUser = new DefaultAuthUser(9L, "admin", null, null, null, List.of("admin"));
        return Base64.encode(JSON.toJSONString(authUser));
    }
}
