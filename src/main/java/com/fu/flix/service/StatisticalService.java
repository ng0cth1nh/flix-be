package com.fu.flix.service;

import com.fu.flix.dto.request.StatisticalCustomerAccountsRequest;
import com.fu.flix.dto.request.StatisticalRepairerAccountsRequest;
import com.fu.flix.dto.response.StatisticalCustomerAccountsResponse;
import com.fu.flix.dto.response.StatisticalRepairerAccountsResponse;
import org.springframework.http.ResponseEntity;

public interface StatisticalService {
    ResponseEntity<StatisticalCustomerAccountsResponse> getStatisticalCustomerAccounts(StatisticalCustomerAccountsRequest request);

    ResponseEntity<StatisticalRepairerAccountsResponse> getStatisticalRepairerAccounts(StatisticalRepairerAccountsRequest request);
}
