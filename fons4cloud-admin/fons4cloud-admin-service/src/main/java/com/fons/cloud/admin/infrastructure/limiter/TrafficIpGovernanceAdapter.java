package com.fons.cloud.admin.infrastructure.limiter;

import com.fons.cloud.admin.api.constants.AdminResultCode;
import com.fons.cloud.admin.api.enums.GovernanceDomain;
import com.fons.cloud.admin.api.response.GovernanceValidateResult;
import com.fons.cloud.admin.api.response.GovernanceValidationMessage;
import com.fons.cloud.admin.domain.adapter.GovernanceTargetAdapter;
import com.fons.cloud.common.base.exception.BizException;
import com.fons.cloud.limiter.api.ManualWhiteIpService;
import com.fons.cloud.limiter.core.BlockDTO;
import com.fons.cloud.limiter.core.ManualBlockedIpService;
import com.fons.cloud.util.JsonUtil;
import com.fons.cloud.web.utils.IpUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 流量治理 IP 黑白名单适配器，复用 limiter 模块已有人工白名单和人工黑名单 Redis 能力。
 */
@Component
public class TrafficIpGovernanceAdapter implements GovernanceTargetAdapter {

    private static final int MIN_BLOCK_SECONDS = 1;

    private final ManualWhiteIpService manualWhiteIpService;
    private final ManualBlockedIpService manualBlockedIpService;

    /**
     * 创建流量治理适配器。
     *
     * @param manualWhiteIpService   人工白名单服务，白名单 IP 永久有效
     * @param manualBlockedIpService 人工黑名单服务，黑名单 IP 按秒设置封禁时间
     */
    public TrafficIpGovernanceAdapter(ManualWhiteIpService manualWhiteIpService,
                                      ManualBlockedIpService manualBlockedIpService) {
        this.manualWhiteIpService = manualWhiteIpService;
        this.manualBlockedIpService = manualBlockedIpService;
    }

    @Override
    public GovernanceDomain domain() {
        return GovernanceDomain.TRAFFIC;
    }

    @Override
    public CurrentConfig loadCurrent(ResourceRef resourceRef) {
        try {
            TrafficIpConfig current = new TrafficIpConfig();
            current.setWhiteIps(new ArrayList<>(manualWhiteIpService.getAllWhiteIp()));
            current.setManualBlockedIps(toBlockedItems(manualBlockedIpService.getAllBlocked()));
            String content = normalize(current);
            return new CurrentConfig(content, hash(content), "limiter:manual-ip-list");
        } catch (RuntimeException ex) {
            throw new BizException(AdminResultCode.ADMIN_TARGET_UNAVAILABLE.getCode(), "读取限流黑白名单失败", ex);
        }
    }

    @Override
    public GovernanceValidateResult validate(TargetConfig targetConfig) {
        List<GovernanceValidationMessage> errors = new ArrayList<>();
        String normalizedContent = null;
        try {
            TrafficIpConfig config = parseConfig(targetConfig.content());
            validateWhiteIps(config.getWhiteIps(), errors);
            validateBlockedIps(config.getManualBlockedIps(), errors);
            normalizedContent = normalize(config);
        } catch (RuntimeException ex) {
            errors.add(message("content", "TRAFFIC_JSON_INVALID", "流量治理配置必须是合法 JSON 对象"));
        }
        return GovernanceValidateResult.builder()
                .passed(errors.isEmpty())
                .errors(errors)
                .warnings(List.of())
                .normalizedContentHash(errors.isEmpty() ? hash(normalizedContent) : null)
                .build();
    }

    @Override
    public AdapterPublishResult publish(TargetConfig targetConfig, PublishContext context) {
        CurrentConfig before = loadCurrent(targetConfig.resourceRef());
        TrafficIpConfig beforeConfig = parseConfig(before.content());
        TrafficIpConfig afterConfig = parseConfig(targetConfig.content());
        String afterContent = normalize(afterConfig);
        try {
            publishWhiteIps(beforeConfig.getWhiteIps(), afterConfig.getWhiteIps());
            publishBlockedIps(beforeConfig.getManualBlockedIps(), afterConfig.getManualBlockedIps());
            CurrentConfig confirmed = loadCurrent(targetConfig.resourceRef());
            return new AdapterPublishResult(true, before.content(), before.contentHash(), confirmed.content(),
                    confirmed.contentHash(), null, null, "limiter 人工黑白名单已刷新");
        } catch (RuntimeException ex) {
            return new AdapterPublishResult(false, before.content(), before.contentHash(), null, null,
                    AdminResultCode.ADMIN_TARGET_UNAVAILABLE.getCode(), ex.getMessage(), null);
        }
    }

    @Override
    public boolean rollbackSupported(ResourceRef resourceRef) {
        return true;
    }

    private void publishWhiteIps(List<String> beforeIps, List<String> afterIps) {
        Set<String> before = normalizeIpSet(beforeIps);
        Set<String> after = normalizeIpSet(afterIps);
        before.stream().filter(ip -> !after.contains(ip)).forEach(manualWhiteIpService::removeWhiteIp);
        after.stream().filter(ip -> !before.contains(ip)).forEach(manualWhiteIpService::addWhiteIp);
    }

    private void publishBlockedIps(List<BlockedIpItem> beforeItems, List<BlockedIpItem> afterItems) {
        Map<String, BlockedIpItem> before = normalizeBlockedMap(beforeItems);
        Map<String, BlockedIpItem> after = normalizeBlockedMap(afterItems);
        before.keySet().stream().filter(ip -> !after.containsKey(ip)).forEach(manualBlockedIpService::removeBlockIp);
        after.forEach((ip, item) -> {
            BlockedIpItem beforeItem = before.get(ip);
            if (beforeItem == null || !Objects.equals(beforeItem.getBlockSeconds(), item.getBlockSeconds())) {
                manualBlockedIpService.addBlockIp(ip, item.getBlockSeconds());
            }
        });
    }

    private void validateWhiteIps(List<String> whiteIps, List<GovernanceValidationMessage> errors) {
        if (CollectionUtils.isEmpty(whiteIps)) {
            return;
        }
        Set<String> exists = new TreeSet<>();
        for (int index = 0; index < whiteIps.size(); index++) {
            String ip = trim(whiteIps.get(index));
            if (!validIp(ip)) {
                errors.add(message("whiteIps[" + index + "]", "TRAFFIC_IP_INVALID", "白名单 IP 格式不合法"));
            } else if (!exists.add(ip)) {
                errors.add(message("whiteIps[" + index + "]", "TRAFFIC_IP_DUPLICATED", "白名单 IP 不能重复"));
            }
        }
    }

    private void validateBlockedIps(List<BlockedIpItem> blockedIps, List<GovernanceValidationMessage> errors) {
        if (CollectionUtils.isEmpty(blockedIps)) {
            return;
        }
        Set<String> exists = new TreeSet<>();
        for (int index = 0; index < blockedIps.size(); index++) {
            BlockedIpItem item = blockedIps.get(index);
            String ip = item == null ? null : trim(item.getIp());
            if (!validIp(ip)) {
                errors.add(message("manualBlockedIps[" + index + "].ip", "TRAFFIC_IP_INVALID", "人工黑名单 IP 格式不合法"));
                continue;
            }
            if (!exists.add(ip)) {
                errors.add(message("manualBlockedIps[" + index + "].ip", "TRAFFIC_IP_DUPLICATED", "人工黑名单 IP 不能重复"));
            }
            if (item.getBlockSeconds() == null || item.getBlockSeconds() < MIN_BLOCK_SECONDS) {
                errors.add(message("manualBlockedIps[" + index + "].blockSeconds", "TRAFFIC_BLOCK_SECONDS_INVALID",
                        "人工黑名单封禁时间必须大于 0 秒"));
            }
        }
    }

    private TrafficIpConfig parseConfig(String content) {
        if (StringUtils.isBlank(content)) {
            return new TrafficIpConfig();
        }
        TrafficIpConfig config = JsonUtil.toBean(content, TrafficIpConfig.class);
        return config == null ? new TrafficIpConfig() : config;
    }

    private String normalize(TrafficIpConfig config) {
        TrafficIpConfig normalized = new TrafficIpConfig();
        normalized.setWhiteIps(new ArrayList<>(normalizeIpSet(config.getWhiteIps())));
        normalized.setManualBlockedIps(new ArrayList<>(normalizeBlockedMap(config.getManualBlockedIps()).values()));
        return JsonUtil.toJson(normalized);
    }

    private Set<String> normalizeIpSet(List<String> ips) {
        if (CollectionUtils.isEmpty(ips)) {
            return new TreeSet<>();
        }
        return ips.stream()
                .map(this::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private Map<String, BlockedIpItem> normalizeBlockedMap(List<BlockedIpItem> items) {
        if (CollectionUtils.isEmpty(items)) {
            return new TreeMap<>();
        }
        return items.stream()
                .filter(Objects::nonNull)
                .filter(item -> StringUtils.isNotBlank(item.getIp()))
                .sorted(Comparator.comparing(BlockedIpItem::getIp))
                .collect(Collectors.toMap(item -> trim(item.getIp()), this::normalizeBlockedItem,
                        (left, right) -> right, TreeMap::new));
    }

    private BlockedIpItem normalizeBlockedItem(BlockedIpItem item) {
        BlockedIpItem normalized = new BlockedIpItem();
        normalized.setIp(trim(item.getIp()));
        normalized.setBlockSeconds(item.getBlockSeconds());
        return normalized;
    }

    private List<BlockedIpItem> toBlockedItems(Map<String, BlockDTO> blockedMap) {
        if (blockedMap == null || blockedMap.isEmpty()) {
            return List.of();
        }
        return blockedMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Function.identity(), (left, right) -> right, TreeMap::new))
                .values().stream()
                .map(entry -> new BlockedIpItem(entry.getKey(), toSeconds(entry.getValue())))
                .toList();
    }

    private Integer toSeconds(BlockDTO blockDTO) {
        if (blockDTO == null || blockDTO.getBlockedMillis() == null) {
            return MIN_BLOCK_SECONDS;
        }
        return Math.max(MIN_BLOCK_SECONDS, (int) Math.ceil(blockDTO.getBlockedMillis() / 1000.0D));
    }

    private boolean validIp(String ip) {
        return StringUtils.isNotBlank(ip) && IpUtil.isIP(ip);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private GovernanceValidationMessage message(String field, String code, String message) {
        return GovernanceValidationMessage.builder()
                .field(field)
                .code(code)
                .message(message)
                .build();
    }

    private String hash(String content) {
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

    /**
     * 流量治理 IP 黑白名单配置。
     */
    @Getter
    @Setter
    public static class TrafficIpConfig {

        /**
         * 人工白名单 IP，永久有效。
         */
        private List<String> whiteIps = List.of();

        /**
         * 人工黑名单 IP，按 `blockSeconds` 控制封禁时间。
         */
        private List<BlockedIpItem> manualBlockedIps = List.of();
    }

    /**
     * 人工黑名单条目。
     */
    @Getter
    @Setter
    public static class BlockedIpItem {

        /**
         * 被封禁 IP。
         */
        private String ip;

        /**
         * 封禁秒数，必须大于 0。
         */
        private Integer blockSeconds;

        public BlockedIpItem() {
        }

        public BlockedIpItem(String ip, Integer blockSeconds) {
            this.ip = ip;
            this.blockSeconds = blockSeconds;
        }
    }
}
