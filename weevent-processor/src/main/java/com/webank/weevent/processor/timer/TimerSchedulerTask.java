package com.webank.weevent.processor.timer;

import com.webank.weevent.processor.model.TimerScheduler;
import com.webank.weevent.processor.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
class TimerSchedulerTask extends TimerTask {
    private TimerScheduler timerScheduler;
    private ReentrantLock reentrantLock = new ReentrantLock();


    public TimerSchedulerTask(TimerScheduler timerScheduler) {
        this.timerScheduler = timerScheduler;
    }

    @Override
    public void run() {
        try(Connection dbcpConnection = CommonUtil.getDbcpConnection(timerScheduler.getJdbcUrl())){
            reentrantLock.lock();
            if(dbcpConnection == null){
             log.error("database connection fail,jdbcUrl:{}",timerScheduler.getJdbcUrl());
         }else {
             PreparedStatement preparedStmt = dbcpConnection.prepareStatement(timerScheduler.getParsingSql());
             boolean execute = preparedStmt.execute();
             if(execute){
                 log.info("execute sql success");
             }
             dbcpConnection.close();
         }
       }catch (Exception e){
            log.error("execute task fail,taskName:{}",timerScheduler.getSchedulerName(),e);
    }finally {
        reentrantLock.unlock();
     }
  }
}
