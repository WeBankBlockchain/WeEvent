package com.webank.weevent.processor.quartz;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.scheduling.quartz.AdaptableJobFactory;
import org.springframework.stereotype.Component;

@Component
public class JobFactory extends AdaptableJobFactory {
    /**
     * AutowireCapableBeanFactory
     */
    private AutowireCapableBeanFactory factory;

    public JobFactory(AutowireCapableBeanFactory factory) {
        this.factory = factory;
    }

    /**
     * create the job
     */
    @Override
    protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {

        Object job = super.createJobInstance(bundle);
        factory.autowireBean(job);
        return job;
    }
}
