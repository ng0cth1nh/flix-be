package com.fu.flix.service;

import com.fu.flix.dto.request.StatisticalCustomerAccountsRequest;
import com.fu.flix.dto.response.StatisticalCustomerAccountsResponse;
import org.springframework.http.ResponseEntity;

public interface StatisticalService {
    ResponseEntity<StatisticalCustomerAccountsResponse> getStatisticalCustomerAccounts(StatisticalCustomerAccountsRequest request);
}
