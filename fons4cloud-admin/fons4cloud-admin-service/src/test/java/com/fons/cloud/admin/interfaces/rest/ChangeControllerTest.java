package com.fons.cloud.admin.interfaces.rest;

import com.fons.cloud.admin.api.response.GovernanceChangeResponse;
import com.fons.cloud.admin.application.ChangeApplicationService;
import com.fons.cloud.admin.application.GovernancePublishService;
import com.fons.cloud.admin.application.GovernanceExecutionRecoveryService;
import com.fons.cloud.common.result.R;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 治理变更 REST API 测试。
 */
@ExtendWith(MockitoExtension.class)
class ChangeControllerTest {

    @Mock
    private ChangeApplicationService changeApplicationService;
    @Mock
    private GovernancePublishService governancePublishService;
    @Mock
    private GovernanceExecutionRecoveryService governanceExecutionRecoveryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ChangeController(changeApplicationService,
                governancePublishService, governanceExecutionRecoveryService)).build();
    }

    @Test
    void queryShouldReturnChangeList() throws Exception {
        when(changeApplicationService.query(any(), any())).thenReturn(R.ok(List.of(response())));

        mockMvc.perform(get("/admin/changes")
                        .param("resourceId", "100")
                        .param("status", "DRAFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].changeNo").value("CHG-001"));
    }

    @Test
    void getByIdShouldReturnChangeDetail() throws Exception {
        when(changeApplicationService.getById(10L)).thenReturn(R.ok(response()));

        mockMvc.perform(get("/admin/changes/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10L));
    }

    private GovernanceChangeResponse response() {
        return GovernanceChangeResponse.builder()
                .id(10L)
                .resourceId(100L)
                .changeNo("CHG-001")
                .changeType("UPDATE")
                .status("DRAFT")
                .baseHash("hash-base")
                .contentHash("hash-content")
                .description("draft")
                .build();
    }
}
