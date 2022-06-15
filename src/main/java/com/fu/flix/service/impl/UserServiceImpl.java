package com.fu.flix.service.impl;


import com.fu.flix.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;


@Service
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {


    public UserServiceImpl() {
    }
}
