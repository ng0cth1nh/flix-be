package com.fu.flix.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InputValidationTest {

    @Test
    void should_valid_phone_number() {
        // given
        String phone = "0865390031";

        // when
        boolean check = InputValidation.isPhoneValid(phone);

        // then
        assertTrue(check);
    }

    @Test
    void should_invalid_phone_number_have_length_greater_than_11() {
        // given
        String phone = "086539003881";

        // when
        boolean check = InputValidation.isPhoneValid(phone);

        // then
        assertFalse(check);
    }

    @Test
    void should_invalid_phone_number_not_start_with_0() {
        // given
        String phone = "3865390031";

        // when
        boolean check = InputValidation.isPhoneValid(phone);

        // then
        assertFalse(check);
    }

    @Test
    void should_invalid_phone_number_have_length_less_than_10() {
        // given
        String phone = "087777777";

        // when
        boolean check = InputValidation.isPhoneValid(phone);

        // then
        assertFalse(check);
    }

    @Test
    void should_valid_password() {
        // given
        String password = "aa1aaa";

        // when
        boolean check = InputValidation.isPasswordValid(password);

        // then
        assertTrue(check);
    }

    @Test
    void should_invalid_password_have_length_less_than_6() {
        // given
        String password = "12#$-";

        // when
        boolean check = InputValidation.isPasswordValid(password);

        // then
        assertFalse(check);
    }

    @Test
    void should_invalid_password_have_length_greater_than_10() {
        // given
        String password = "12#$-123457";

        // when
        boolean check = InputValidation.isPasswordValid(password);

        // then
        assertFalse(check);
    }

    @Test
    void should_invalid_password_have_whitespace() {
        // given
        String password = "123456 7";

        // when
        boolean check = InputValidation.isPasswordValid(password);

        // then
        assertFalse(check);
    }

    @Test
    void should_valid_email() {
        // given
        String email = "dung6app@gmail.com";

        // when
        boolean check = InputValidation.isEmailValid(email, false);

        // then
        Assertions.assertTrue(check);
    }

    @Test
    void should_valid_email_when_email_is_nullable() {
        // then
        Assertions.assertTrue(InputValidation.isEmailValid(null, true));
    }

    @Test
    void should_not_valid_email_when_email_is_not_nullable() {
        // then
        Assertions.assertFalse(InputValidation.isEmailValid(null, false));
    }

    @Test
    void should_valid_email_when_email_is_not_null_but_nullable() {
        // given
        String email = "dung6app@gmail.com";

        // when
        boolean check = InputValidation.isEmailValid(email, true);

        // then
        Assertions.assertTrue(check);
    }

    @Test
    void should_valid_full_name() {
        // given
        String fullName = "Doan U";

        // when
        boolean check = InputValidation.isNameValid(fullName, 250L);

        // then
        Assertions.assertTrue(check);
    }
}