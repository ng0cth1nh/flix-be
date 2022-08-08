package com.fu.flix.job.impl;

import com.fu.flix.util.DateFormatUtil;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
class CronJobImplTest {
    private final String NOTIFICATION_DATE_TIME_PATTERN = "HH:mm - dd/MM/yyyy";

    @Test
    void cancelApprovalRequestAutomatically() {
        System.out.println(DateFormatUtil.toString(LocalDateTime.now(), NOTIFICATION_DATE_TIME_PATTERN));
    }
}