package com.fons.cloud.admin.interfaces.rest;

import com.fons.cloud.admin.api.constants.AdminPermissionCodes;
import com.fons.cloud.admin.api.response.ServiceInstanceResponse;
import com.fons.cloud.admin.infrastructure.discovery.ServiceDiscoveryReadAdapter;
import com.fons.cloud.admin.infrastructure.security.AdminPermission;
import com.fons.cloud.auth.annotation.AuthenticationResource;
import com.fons.cloud.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 服务治理只读 REST API。
 */
@RestController
@RequestMapping("/admin/services")
@RequiredArgsConstructor
public class ServiceGovernanceController {

    private final ServiceDiscoveryReadAdapter serviceDiscoveryReadAdapter;

    /**
     * 查询注册中心服务名列表。
     *
     * @return 服务名列表
    */
    @GetMapping
    @AuthenticationResource(authorities = "ADMIN")
    @AdminPermission(authorities = AdminPermissionCodes.SERVICES_VIEW)
    public R<List<String>> listServices() {
        return R.ok(serviceDiscoveryReadAdapter.listServices());
    }

    /**
     * 查询指定服务的实例列表。
     *
     * @param serviceName 注册中心服务名
     * @return 服务实例只读视图
    */
    @GetMapping("/{serviceName}/instances")
    @AuthenticationResource(authorities = "ADMIN")
    @AdminPermission(authorities = AdminPermissionCodes.SERVICES_VIEW)
    public R<List<ServiceInstanceResponse>> listInstances(@PathVariable String serviceName) {
        return R.ok(serviceDiscoveryReadAdapter.listInstances(serviceName));
    }
}
