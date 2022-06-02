package com.fpt.flix.flix_app.controllers;


import com.fpt.flix.flix_app.services.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@RestController
@Slf4j
@RequestMapping("api/v1/customer")
public class CustomerController {
    private final AccountService accountService;

    public CustomerController(AccountService accountService
    ) {
        this.accountService = accountService;
    }

}
