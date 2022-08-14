package com.fu.flix.util;

import com.fu.flix.constant.enums.StatisticalDateType;
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

    @Test
    void should_valid_bank_name() {
        // given
        String bankName = "CHI DUNG";

        // when
        boolean check = InputValidation.isBankNameValid(bankName, true);

        // then
        Assertions.assertTrue(check);
    }

    @Test
    void should_invalid_bank_name() {
        // given
        String bankName = "CHI DUNg";

        // when
        boolean check = InputValidation.isBankNameValid(bankName, true);

        // then
        Assertions.assertFalse(check);
    }

    @Test
    void should_valid_bank_name_when_ban_name_is_null_and_allow_nullable() {
        Assertions.assertTrue(InputValidation.isBankNameValid(null, true));
    }

    @Test
    void should_invalid_bank_name_when_ban_name_is_null_and_not_allow_nullable() {
        Assertions.assertFalse(InputValidation.isBankNameValid(null, false));
    }

    @Test
    void should_valid_bank_number_when_length_is_8() {
        // given
        String bankNumber = "12345678";

        // when
        boolean check = InputValidation.isBankNumberValid(bankNumber, true);

        // then
        Assertions.assertTrue(check);
    }

    @Test
    void should_valid_bank_number_when_length_is_17() {
        // given
        String bankNumber = "12345678912345678";

        // when
        boolean check = InputValidation.isBankNumberValid(bankNumber, true);

        // then
        Assertions.assertTrue(check);
    }

    @Test
    void should_invalid_bank_number_when_length_is_7() {
        // given
        String bankNumber = "1234567";

        // when
        boolean check = InputValidation.isBankNumberValid(bankNumber, true);

        // then
        Assertions.assertFalse(check);
    }

    @Test
    void should_invalid_bank_number_when_length_is_18() {
        // given
        String bankNumber = "123456789123456789";

        // when
        boolean check = InputValidation.isBankNumberValid(bankNumber, true);

        // then
        Assertions.assertFalse(check);
    }

    @Test
    void should_invalid_bank_number_when_length_contain_character() {
        // given
        String bankNumber = "1234567abc";

        // when
        boolean check = InputValidation.isBankNumberValid(bankNumber, true);

        // then
        Assertions.assertFalse(check);
    }

    @Test
    void test_isMatchQueryDateType_when_type_is_day_success() {
        // given
        String dateStr = "20/03/2000";
        StatisticalDateType type = StatisticalDateType.DAY;

        // when
        boolean matchQueryDateType = InputValidation.isMatchQueryDateType(dateStr, type);

        // then
        Assertions.assertTrue(matchQueryDateType);
    }

    @Test
    void test_isMatchQueryDateType_when_type_is_day_fail() {
        // given
        String dateStr = "20/3/2000";
        StatisticalDateType type = StatisticalDateType.DAY;

        // when
        boolean matchQueryDateType = InputValidation.isMatchQueryDateType(dateStr, type);

        // then
        Assertions.assertFalse(matchQueryDateType);
    }

    @Test
    void test_isMatchQueryDateType_when_type_is_month_success() {
        // given
        String dateStr = "23/2000";
        StatisticalDateType type = StatisticalDateType.MONTH;

        // when
        boolean matchQueryDateType = InputValidation.isMatchQueryDateType(dateStr, type);

        // then
        Assertions.assertTrue(matchQueryDateType);
    }

    @Test
    void test_isMatchQueryDateType_when_type_is_month_fail() {
        // given
        String dateStr = "3/2000";
        StatisticalDateType type = StatisticalDateType.MONTH;

        // when
        boolean matchQueryDateType = InputValidation.isMatchQueryDateType(dateStr, type);

        // then
        Assertions.assertFalse(matchQueryDateType);
    }

    @Test
    void test_isMatchQueryDateType_when_type_is_year_success() {
        // given
        String dateStr = "2000";
        StatisticalDateType type = StatisticalDateType.YEAR;

        // when
        boolean matchQueryDateType = InputValidation.isMatchQueryDateType(dateStr, type);

        // then
        Assertions.assertTrue(matchQueryDateType);
    }

    @Test
    void test_isMatchQueryDateType_when_type_is_year_month_fail() {
        // given
        String dateStr = "200";
        StatisticalDateType type = StatisticalDateType.YEAR;

        // when
        boolean matchQueryDateType = InputValidation.isMatchQueryDateType(dateStr, type);

        // then
        Assertions.assertFalse(matchQueryDateType);
    }
}