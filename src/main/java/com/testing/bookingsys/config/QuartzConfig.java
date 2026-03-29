package com.testing.bookingsys.config;

import com.testing.bookingsys.scheduler.WaitlistRefundJob;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetailFactoryBean waitlistRefundJobDetail() {
        JobDetailFactoryBean factory = new JobDetailFactoryBean();
        factory.setJobClass(WaitlistRefundJob.class);
        factory.setDescription("Refund credits for waitlist entries after class end");
        factory.setDurability(true);
        return factory;
    }

    @Bean
    public SimpleTriggerFactoryBean waitlistRefundTrigger(JobDetail waitlistRefundJobDetail) {
        SimpleTriggerFactoryBean factory = new SimpleTriggerFactoryBean();
        factory.setJobDetail(waitlistRefundJobDetail);
        factory.setRepeatInterval(300000L);
        factory.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        return factory;
    }
}
