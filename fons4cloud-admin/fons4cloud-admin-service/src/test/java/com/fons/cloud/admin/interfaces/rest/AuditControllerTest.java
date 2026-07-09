package com.fons.cloud.admin.interfaces.rest;

import com.fons.cloud.admin.api.response.GovernanceAuditResponse;
import com.fons.cloud.admin.application.AuditApplicationService;
import com.fons.cloud.common.result.R;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * admin 审计 REST API 测试。
 */
@ExtendWith(MockitoExtension.class)
class AuditControllerTest {

    @Mock
    private AuditApplicationService auditApplicationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuditController(auditApplicationService)).build();
    }

    @Test
    void queryShouldReturnAuditList() throws Exception {
        when(auditApplicationService.query(any())).thenReturn(R.ok(List.of(response())));

        mockMvc.perform(get("/admin/audits")
                        .param("domain", "ACCESS")
                        .param("operation", "ROLE_GRANT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].operation").value("ROLE_GRANT"));
    }

    @Test
    void getByIdShouldReturnAuditDetail() throws Exception {
        when(auditApplicationService.getById(1L)).thenReturn(R.ok(response()));

        mockMvc.perform(get("/admin/audits/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    private GovernanceAuditResponse response() {
        return GovernanceAuditResponse.builder()
                .id(1L)
                .domain("access")
                .operation("ROLE_GRANT")
                .result("SUCCESS")
                .detailSummary("role grant")
                .operatedAt(new Date())
                .build();
    }
}
