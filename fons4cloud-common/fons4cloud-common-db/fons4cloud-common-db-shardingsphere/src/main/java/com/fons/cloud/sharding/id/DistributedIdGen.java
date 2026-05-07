package com.fons.cloud.sharding.id;

import cn.hutool.core.util.IdUtil;
import com.fons.cloud.sharding.strategy.ShardingTableStrategy;
import com.fons.cloud.sharding.strategy.support.DefaultShardingTableStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * 分布式id成器
 * @author qiyuan.hong
 * @version 1.0
 * @date 2023/3/22 15:18
 */
@Slf4j
public class DistributedIdGen {

    /**
     * 默认的分表策略.
     */
    public static ShardingTableStrategy strategy = new DefaultShardingTableStrategy();


    public static long getSnowflakeId(long workerId) {
        return IdUtil.getSnowflake(workerId).nextId();
    }

    public static String generateWithSnowflake(BusinessCode businessCode, long workerId, long externalId) {
        return generateWithSnowflake(businessCode.getBusinessCode(), businessCode.getTableCount(), workerId, externalId);
    }


    public static String generateWithSnowflake(String businessCode, int tableCount, long workerId, long externalId) {
        long seq = IdUtil.getSnowflake(workerId).nextId();
        return generate(businessCode, tableCount, externalId, seq);
    }

    /**
     * 生成分布式id 由业务code + 外部id（机器序列号） + 表下标
     * @param businessCode   业务code， 应该为数值类型
     * @param tableCount     分表的数目
     * @param externalId     外部id
     * @param sequenceNumber 机器序列号
     * @return               分布式id
     */
    public static String generate(String businessCode, int tableCount, long externalId, long sequenceNumber) {
        tableCount = tableCount > 0 ? tableCount : 1;
        // 表下标.
        String tableIndex = String.valueOf(strategy.getTableIndex(String.valueOf(externalId), tableCount));
        DistributedID id = DistributedID.create(String.valueOf(businessCode), sequenceNumber, tableIndex);
        return id.toString();
    }







}
