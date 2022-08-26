package com.fu.flix.service.impl;

import com.fu.flix.configuration.AppConf;
import com.fu.flix.dao.*;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.transaction.Transactional;

import java.util.Optional;

import static com.fu.flix.constant.Constant.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@Transactional
class ValidatorServiceImplTest {
    @InjectMocks
    ValidatorServiceImpl underTest;
    @Mock
    UserDAO userDAO;
    @Mock
    AppConf appConf;
    @Mock
    ServiceDAO serviceDAO;
    @Mock
    CategoryDAO categoryDAO;
    @Mock
    SubServiceDAO subServiceDAO;
    @Mock
    RepairRequestDAO repairRequestDAO;
    @Mock
    AccessoryDAO accessoryDAO;
    @Mock
    FeedbackDAO feedbackDAO;
    @Mock
    TransactionHistoryDAO transactionHistoryDAO;
    @Mock
    User user;

    @BeforeEach
    void setup() {
        user = new User();
    }

    @Test
    void test_getUserValidated_when_username_is_empty() {
        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getUserValidated(""));

        // then
        Assertions.assertEquals(USER_NAME_IS_REQUIRED, exception.getMessage());
    }

    @Test
    void test_getUserValidated_when_user_id_is_null() {
        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getUserValidated((Long) null));

        // then
        Assertions.assertEquals(USER_ID_IS_REQUIRED, exception.getMessage());
    }

    @Test
    void test_getUserValidated_when_user_inactive() {
        // when
        user.setIsActive(false);
        Mockito.when(userDAO.findById(36L)).thenReturn(Optional.of(user));
        Exception exception = Assertions.assertThrows(GeneralException.class,
                () -> underTest.getUserValidated(36L));

        // then
        Assertions.assertEquals(USER_IS_INACTIVE, exception.getMessage());
    }

    @Test
    void test_getPageSize_fail_when_pageSize_less_than_1(){
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getPageSize(0));
        Assertions.assertEquals(PAGE_SIZE_MUST_BE_GREATER_OR_EQUAL_1, exception.getMessage());
    }

    @Test
    void test_getPageSize_fail_when_pageNumber_less_than_0(){
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getPageNumber(-1));
        Assertions.assertEquals(PAGE_NUMBER_MUST_BE_GREATER_OR_EQUAL_0, exception.getMessage());
    }

    @Test
    void test_getCategoryValidated_fail_when_id_is_null() {
        Exception exception = Assertions.assertThrows(GeneralException.class, () ->  underTest.getCategoryValidated(null));
        Assertions.assertEquals(INVALID_CATEGORY, exception.getMessage());
    }

    @Test
    void test_getCategoryValidated_fail_when_id_is_invalid() {
        Mockito.when( categoryDAO.findById(1L)).thenReturn(Optional.empty());
        Exception exception = Assertions.assertThrows(GeneralException.class, () ->  underTest.getCategoryValidated(1L));
        Assertions.assertEquals(INVALID_CATEGORY, exception.getMessage());
    }
}