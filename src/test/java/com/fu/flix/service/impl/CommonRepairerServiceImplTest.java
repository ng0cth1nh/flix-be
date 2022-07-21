package com.fu.flix.service.impl;

import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.RequestingRepairRequest;
import com.fu.flix.dto.request.RequestingSuggestionRequest;
import com.fu.flix.dto.response.RequestingRepairResponse;
import com.fu.flix.dto.response.RequestingSuggestionResponse;
import com.fu.flix.dto.security.UserPrincipal;
import com.fu.flix.service.CommonRepairerService;
import com.fu.flix.service.CustomerService;
import com.fu.flix.util.DateFormatUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

import static com.fu.flix.constant.Constant.INVALID_REPAIRER_SUGGESTION_TYPE;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
class CommonRepairerServiceImplTest {

    @Autowired
    CommonRepairerService underTest;

    String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    CustomerService customerService;

    @Test
    void getSuggestionRequestList_success_when_type_is_SUGGESTED() {
        // given
        RequestingSuggestionRequest request = new RequestingSuggestionRequest();
        request.setType("SUGGESTED");

        // when
        createFixingRequest(36L, "0865390037");
        setRepairerContext(52L, "0865390052");
        RequestingSuggestionResponse response = underTest.getSuggestionRequestList(request).getBody();

        // then
        Assertions.assertNotNull(response.getRequestList());
    }

    @Test
    void getSuggestionRequestList_success_when_type_is_INTERESTED() {
        // given
        RequestingSuggestionRequest request = new RequestingSuggestionRequest();
        request.setType("INTERESTED");

        // when
        createFixingRequest(36L, "0865390037");
        setRepairerContext(52L, "0865390052");
        RequestingSuggestionResponse response = underTest.getSuggestionRequestList(request).getBody();

        // then
        Assertions.assertNotNull(response.getRequestList());
    }

    @Test
    void getSuggestionRequestList_fail_when_type_is_empty() {
        // given
        RequestingSuggestionRequest request = new RequestingSuggestionRequest();
        request.setType("");

        // when
        setRepairerContext(52L, "0865390052");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getSuggestionRequestList(request).getBody());

        // then
        Assertions.assertEquals(INVALID_REPAIRER_SUGGESTION_TYPE, exception.getMessage());
    }

    @Test
    void getSuggestionRequestList_fail_when_type_is_null() {
        // given
        RequestingSuggestionRequest request = new RequestingSuggestionRequest();
        request.setType(null);

        // when
        setRepairerContext(52L, "0865390052");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getSuggestionRequestList(request).getBody());

        // then
        Assertions.assertEquals(INVALID_REPAIRER_SUGGESTION_TYPE, exception.getMessage());
    }

    @Test
    void getSuggestionRequestList_fail_when_type_is_wrong() {
        // given
        RequestingSuggestionRequest request = new RequestingSuggestionRequest();
        request.setType("abc");

        // when
        setRepairerContext(52L, "0865390052");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getSuggestionRequestList(request).getBody());

        // then
        Assertions.assertEquals(INVALID_REPAIRER_SUGGESTION_TYPE, exception.getMessage());
    }

    void setRepairerContext(Long id, String phone) {
        String[] roles = {"ROLE_REPAIRER"};
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(new UserPrincipal(id, phone, roles), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private String createFixingRequest(Long userId, String phone) {
        setUserContext(userId, phone);
        Long serviceId = 1L;
        Long addressId = 7L;
        String expectFixingDay = DateFormatUtil.toString(LocalDateTime.now().plusDays(2L), DATE_TIME_PATTERN);
        String description = "Thợ phải đẹp trai";
        Long voucherId = 1L;
        String paymentMethodId = "C";

        RequestingRepairRequest request = new RequestingRepairRequest();
        request.setServiceId(serviceId);
        request.setVoucherId(voucherId);
        request.setDescription(description);
        request.setExpectFixingDay(expectFixingDay);
        request.setAddressId(addressId);
        request.setPaymentMethodId(paymentMethodId);

        RequestingRepairResponse response = customerService.createFixingRequest(request).getBody();
        return response.getRequestCode();
    }

    void setUserContext(Long id, String phone) {
        String[] roles = {"ROLE_CUSTOMER"};
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(new UserPrincipal(id, phone, roles), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}