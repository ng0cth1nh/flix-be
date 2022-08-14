package com.fu.flix.service;

import com.fu.flix.dto.request.StatisticalCustomerAccountsRequest;
import com.fu.flix.dto.request.StatisticalRepairerAccountsRequest;
import com.fu.flix.dto.request.StatisticalRequestsRequest;
import com.fu.flix.dto.request.StatisticalTransactionsRequest;
import com.fu.flix.dto.response.StatisticalCustomerAccountsResponse;
import com.fu.flix.dto.response.StatisticalRepairerAccountsResponse;
import com.fu.flix.dto.response.StatisticalRequestsResponse;
import com.fu.flix.dto.response.StatisticalTransactionsResponse;
import org.springframework.http.ResponseEntity;

public interface StatisticalService {
    ResponseEntity<StatisticalCustomerAccountsResponse> getStatisticalCustomerAccounts(StatisticalCustomerAccountsRequest request);

    ResponseEntity<StatisticalRepairerAccountsResponse> getStatisticalRepairerAccounts(StatisticalRepairerAccountsRequest request);

    ResponseEntity<StatisticalRequestsResponse> getStatisticalRequests(StatisticalRequestsRequest request);

    ResponseEntity<StatisticalTransactionsResponse> getStatisticalTransactions(StatisticalTransactionsRequest request);
}
