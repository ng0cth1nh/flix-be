package com.fu.flix.service.impl;

import com.fu.flix.constant.enums.RoleType;
import com.fu.flix.constant.enums.StatisticalDateType;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.StatisticalCustomerAccountsRequest;
import com.fu.flix.dto.request.StatisticalRepairerAccountsRequest;
import com.fu.flix.dto.request.StatisticalRequestsRequest;
import com.fu.flix.dto.request.StatisticalTransactionsRequest;
import com.fu.flix.dto.response.StatisticalCustomerAccountsResponse;
import com.fu.flix.dto.response.StatisticalRepairerAccountsResponse;
import com.fu.flix.dto.response.StatisticalRequestsResponse;
import com.fu.flix.dto.response.StatisticalTransactionsResponse;
import com.fu.flix.dto.security.UserPrincipal;
import com.fu.flix.service.StatisticalService;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.fu.flix.constant.Constant.INVALID_DATE_TYPE;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
class StatisticalServiceImplTest {

    @Autowired
    StatisticalService underTest;

    @Test
    void test_getStatisticalCustomerAccounts_success_when_type_is_day() {
        // given
        StatisticalCustomerAccountsRequest request = new StatisticalCustomerAccountsRequest();
        request.setType(StatisticalDateType.DAY.name());
        request.setFrom("20/07/2022");
        request.setTo("20/07/2022");

        setManagerContext(438L, "0865390063");

        // when
        StatisticalCustomerAccountsResponse response = underTest.getStatisticalCustomerAccounts(request).getBody();

        // then
        Assertions.assertNotNull(response.getData());
    }

    @Test
    void test_getStatisticalCustomerAccounts_success_when_type_is_month() {
        // given
        StatisticalCustomerAccountsRequest request = new StatisticalCustomerAccountsRequest();
        request.setType(StatisticalDateType.MONTH.name());
        request.setFrom("07/2022");
        request.setTo("07/2022");

        setManagerContext(438L, "0865390063");

        // when
        StatisticalCustomerAccountsResponse response = underTest.getStatisticalCustomerAccounts(request).getBody();

        // then
        Assertions.assertNotNull(response.getData());
    }


    @Test
    void test_getStatisticalCustomerAccounts_success_when_type_is_year() {
        // given
        StatisticalCustomerAccountsRequest request = new StatisticalCustomerAccountsRequest();
        request.setType(StatisticalDateType.YEAR.name());
        request.setFrom("2022");
        request.setTo("2022");

        setManagerContext(438L, "0865390063");

        // when
        StatisticalCustomerAccountsResponse response = underTest.getStatisticalCustomerAccounts(request).getBody();

        // then
        Assertions.assertNotNull(response.getData());
    }

    @Test
    void test_getStatisticalRepairerAccounts_success_when_type_is_day() {
        // given
        StatisticalRepairerAccountsRequest request = new StatisticalRepairerAccountsRequest();
        request.setType(StatisticalDateType.DAY.name());
        request.setFrom("20/07/2022");
        request.setTo("20/07/2022");

        setManagerContext(438L, "0865390063");

        // when
        StatisticalRepairerAccountsResponse response = underTest.getStatisticalRepairerAccounts(request).getBody();

        // then
        Assertions.assertNotNull(response.getData());
    }

    @Test
    void test_getStatisticalRepairerAccounts_success_when_type_is_month() {
        // given
        StatisticalRepairerAccountsRequest request = new StatisticalRepairerAccountsRequest();
        request.setType(StatisticalDateType.MONTH.name());
        request.setFrom("07/2022");
        request.setTo("07/2022");

        setManagerContext(438L, "0865390063");

        // when
        StatisticalRepairerAccountsResponse response = underTest.getStatisticalRepairerAccounts(request).getBody();

        // then
        Assertions.assertNotNull(response.getData());
    }


    @Test
    void test_getStatisticalRepairerAccounts_success_when_type_is_year() {
        // given
        StatisticalRepairerAccountsRequest request = new StatisticalRepairerAccountsRequest();
        request.setType(StatisticalDateType.YEAR.name());
        request.setFrom("2022");
        request.setTo("2022");

        setManagerContext(438L, "0865390063");

        // when
        StatisticalRepairerAccountsResponse response = underTest.getStatisticalRepairerAccounts(request).getBody();

        // then
        Assertions.assertNotNull(response.getData());
    }

    @Test
    void test_getStatisticalRequests_success_when_type_is_day() {
        // given
        StatisticalRequestsRequest request = new StatisticalRequestsRequest();
        request.setType(StatisticalDateType.DAY.name());
        request.setFrom("20/07/2022");
        request.setTo("20/07/2022");

        setManagerContext(438L, "0865390063");

        // when
        StatisticalRequestsResponse response = underTest.getStatisticalRequests(request).getBody();

        // then
        Assertions.assertNotNull(response.getData());
    }

    @Test
    void test_getStatisticalRequests_success_when_type_is_month() {
        // given
        StatisticalRequestsRequest request = new StatisticalRequestsRequest();
        request.setType(StatisticalDateType.MONTH.name());
        request.setFrom("07/2022");
        request.setTo("07/2022");

        setManagerContext(438L, "0865390063");

        // when
        StatisticalRequestsResponse response = underTest.getStatisticalRequests(request).getBody();

        // then
        Assertions.assertNotNull(response.getData());
    }


    @Test
    void test_getStatisticalRequests_success_when_type_is_year() {
        // given
        StatisticalRequestsRequest request = new StatisticalRequestsRequest();
        request.setType(StatisticalDateType.YEAR.name());
        request.setFrom("2022");
        request.setTo("2022");

        setManagerContext(438L, "0865390063");

        // when
        StatisticalRequestsResponse response = underTest.getStatisticalRequests(request).getBody();

        // then
        Assertions.assertNotNull(response.getData());
    }

    @Test
    void test_getStatisticalTransactions_success_when_type_is_day() {
        // given
        StatisticalTransactionsRequest request = new StatisticalTransactionsRequest();
        request.setType(StatisticalDateType.DAY.name());
        request.setFrom("20/07/2022");
        request.setTo("20/07/2022");

        setManagerContext(438L, "0865390063");

        // when
        StatisticalTransactionsResponse response = underTest.getStatisticalTransactions(request).getBody();

        // then
        Assertions.assertNotNull(response.getData());
    }

    @Test
    void test_getStatisticalTransactions_success_when_type_is_month() {
        // given
        StatisticalTransactionsRequest request = new StatisticalTransactionsRequest();
        request.setType(StatisticalDateType.MONTH.name());
        request.setFrom("07/2022");
        request.setTo("07/2022");

        setManagerContext(438L, "0865390063");

        // when
        StatisticalTransactionsResponse response = underTest.getStatisticalTransactions(request).getBody();

        // then
        Assertions.assertNotNull(response.getData());
    }


    @Test
    void test_getStatisticalTransactions_success_when_type_is_year() {
        // given
        StatisticalTransactionsRequest request = new StatisticalTransactionsRequest();
        request.setType(StatisticalDateType.YEAR.name());
        request.setFrom("2022");
        request.setTo("2022");

        setManagerContext(438L, "0865390063");

        // when
        StatisticalTransactionsResponse response = underTest.getStatisticalTransactions(request).getBody();

        // then
        Assertions.assertNotNull(response.getData());
    }

    @Test
    void test_getStatisticalTransactions_fail_when_type_is_invalid() {
        // given
        StatisticalTransactionsRequest request = new StatisticalTransactionsRequest();
        request.setType("MEOMEO");
        request.setFrom("2022");
        request.setTo("2022");

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getStatisticalTransactions(request));

        // then
        Assertions.assertEquals(INVALID_DATE_TYPE, exception.getMessage());
    }

    void setManagerContext(Long id, String phone) {
        List<String> roles = new ArrayList<>();
        roles.add(RoleType.ROLE_MANAGER.name());
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(new UserPrincipal(id, phone, roles), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}