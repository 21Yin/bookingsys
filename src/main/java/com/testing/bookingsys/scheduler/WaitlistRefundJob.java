package com.testing.bookingsys.scheduler;

import com.testing.bookingsys.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WaitlistRefundJob implements Job {

    private final BookingService bookingService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        bookingService.refundExpiredWaitlists();
    }
}
