package com.fons.cloud.datasource.druid.filter;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.alibaba.druid.stat.DruidStatManagerFacade;
import com.fons.cloud.common.base.lang.DateMeasureConstants;
import com.fons.cloud.common.base.lang.NumberConstants;
import com.fons.cloud.common.swticher.CommonSwitcher;
import com.fons.cloud.util.concurrent.IExecutorsRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 拓展 {@link com.alibaba.druid.filter.stat.StatFilter}
 * 新增处理慢sql/错sql事件
 * @author qiyuan.hong
 * @version 1.0
 * @date 2023/12/8
 */
@Slf4j
public class ExtendDruidStatFilter extends StatFilter {
    private static final String SCHEDULE_NAME = "druid-states-reset";
    private static final AtomicLong COUNTER = new AtomicLong(1);
    private static final int FREQUENCY = 60;

    public ExtendDruidStatFilter(StatFilterConfig statFilterConfig) {
        super.setSlowSqlMillis(statFilterConfig.slowSqlMillis());
        super.setLogSlowSql(statFilterConfig.logSlowSql());
        super.setMergeSql(statFilterConfig.mergeSql());
        // 启动重置druid stat数据job 防止记录的sql数据太多导致oom.
        startResetStatDataJob();
    }

    private void startResetStatDataJob() {
        ScheduledExecutorService service = IExecutorsRepository.newSingleScheduledExecutor(SCHEDULE_NAME);
        service.scheduleAtFixedRate(this::doResetStatData, DateMeasureConstants.FIVE_MINUTES.toMillis(), DateMeasureConstants.ONE_MINUTES.toMillis(), TimeUnit.MILLISECONDS);
    }

    private void doResetStatData() {
        long count = COUNTER.incrementAndGet();
        try {
            if (CommonSwitcher.ENABLE_SCHEDULE_RESET_DRUID_STATES.isOff()) {
                log.info("Switcher enable reset druid state is off.");
            } else {
                if (count % FREQUENCY == 0) {
                    // 重置所有数据.
                    DruidStatManagerFacade.getInstance().resetAll();
                }
            }
        } catch (Throwable cause) {
            log.error(cause.getMessage(), cause);
        }
    }

    @Override
    protected void statement_executeErrorAfter(StatementProxy statement, String sql, Throwable error) {
        // 发生异常之后, 处理错误的sql
        handlerErrorSql(statement, sql, error);
        super.statement_executeErrorAfter(statement, sql, error);
    }

    private void handlerErrorSql(StatementProxy statement, String sql, Throwable error) {
        try {
            String params = this.buildSlowParameters(statement);
            long costMills = statement.getLastExecuteTimeNano() / NumberConstants.ONE_NANO_4MILLISECONDS;
            log.warn("Handler error sql. sql: {} | cost millis: {}.", sql, costMills);
        } catch (Throwable cause) {
            log.error(cause.getMessage(), cause);
        }
    }


    @Override
    protected void handleSlowSql(StatementProxy statementProxy) {
        try {
            // 是否开启慢SQL采集
            String sql = statementProxy.getLastExecuteSql();
            long costMills = statementProxy.getLastExecuteTimeNano() / NumberConstants.ONE_NANO_4MILLISECONDS;
            log.warn("Handler slow sql. sql: {} | cost millis: {}.", sql, costMills);
        } catch (Throwable cause) {
            log.error(cause.getMessage(), cause);
        }
    }




}
