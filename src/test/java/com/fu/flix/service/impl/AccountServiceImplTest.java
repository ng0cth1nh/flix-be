package com.fu.flix.service.impl;

import com.fu.flix.service.AccountService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
class AccountServiceImplTest {

    @Autowired
    AccountService accountService;
}