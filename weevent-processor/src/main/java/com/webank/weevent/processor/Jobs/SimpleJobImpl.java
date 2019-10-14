package com.webank.weevent.processor.jobs;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleJobImpl implements SimpleJob {
    @Override
    public void execute(ShardingContext context) {
        System.out.println(String.format("Thread ID: %s, 任务总片数: %s, 当前分片项: %s, 分片参数: %s",
                Thread.currentThread().getId(), context.getShardingTotalCount(), context.getShardingItem(), context.getShardingParameter()));

        switch (context.getShardingItem()) {
            case 0:
                // do something by sharding item 0
                break;
            case 1:
                // do something by sharding item 1
                break;
            case 2:
                // do something by sharding item 2
                break;
            // case n: ...
        }
    }
}

