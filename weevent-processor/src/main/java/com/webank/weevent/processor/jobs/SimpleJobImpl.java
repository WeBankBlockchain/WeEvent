package com.webank.weevent.processor.jobs;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleJobImpl implements SimpleJob {
    @Override
    public void execute(ShardingContext shardingContext) {
        log.info("Thread ID: {},ShardingTotalCount:{},getJobName:{},getShardingItem:{},getShardingParameter:{}", Thread.currentThread().getId(), shardingContext.getShardingTotalCount(), shardingContext.getJobName(), shardingContext.getShardingItem(), shardingContext.getShardingParameter());
        int shardingItem = shardingContext.getShardingItem();
        switch (shardingItem) {
            case 0:
                log.info("000000");
                break;

            case 1:
                log.info("11111");
                break;

            case 2:
                log.info("22222");
                break;

            default:
                log.info("default");
        }
    }

}

