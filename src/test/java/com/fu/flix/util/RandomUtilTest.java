package com.fu.flix.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class RandomUtilTest {

    @Autowired
    RandomUtil randomUtil;

    @Test
    void generateCode() {
        System.out.println(randomUtil.generateCode());
        System.out.println(randomUtil.generateCode());
        System.out.println(randomUtil.generateCode());
        System.out.println(randomUtil.generateCode());
    }
}