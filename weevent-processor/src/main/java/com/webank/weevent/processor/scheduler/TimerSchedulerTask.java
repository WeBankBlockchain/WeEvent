package com.webank.weevent.processor.scheduler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.TimerTask;

import com.webank.weevent.processor.model.TimerScheduler;
import com.webank.weevent.processor.utils.CommonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class TimerSchedulerTask extends TimerTask implements AutoCloseable {
    private TimerScheduler timerScheduler;

    public TimerSchedulerTask(TimerScheduler timerScheduler) {
        this.timerScheduler = timerScheduler;
    }


    /**
     * todo
     * 1.CRUD的测试用例，8个
     * 2.CRUD流程测完
     * 3.这个怎么测
     *
     */
    public void run() {
        /**
         * 1。连接数据库
         * 2.执行sql
         * 3.governance的查询历史维度数据失败
         */
        try {
            try (Connection conn = CommonUtil.getDbcpConnection(timerScheduler.getJdbcUrl())) {
                if (conn == null) {
                    log.error("database connection failed");
                } else {
                    log.info("sql:{}", timerScheduler.getParsingSql());
                    PreparedStatement preparedStmt = conn.prepareStatement(timerScheduler.getParsingSql());
                    int update = preparedStmt.executeUpdate();
                    if (update > 0) {
                        log.info("parse success");
                    }
                }
            } catch (Exception e) {
                log.info("");
            }
        } catch (Exception e) {
            log.info("");
        }
    }

    @Override
    public void close() throws Exception {

    }
}
