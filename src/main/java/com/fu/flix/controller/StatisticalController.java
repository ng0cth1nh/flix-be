package com.fu.flix.controller;

import com.fu.flix.dto.request.StatisticalCustomerAccountsRequest;
import com.fu.flix.dto.request.StatisticalRepairerAccountsRequest;
import com.fu.flix.dto.request.StatisticalRequestsRequest;
import com.fu.flix.dto.response.StatisticalCustomerAccountsResponse;
import com.fu.flix.dto.response.StatisticalRepairerAccountsResponse;
import com.fu.flix.dto.response.StatisticalRequestsResponse;
import com.fu.flix.service.StatisticalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/admin/dashboard")
public class StatisticalController {
    private final StatisticalService statisticalService;

    public StatisticalController(StatisticalService statisticalService) {
        this.statisticalService = statisticalService;
    }

    @GetMapping("account/customers")
    public ResponseEntity<StatisticalCustomerAccountsResponse> getStatisticalCustomerAccounts(StatisticalCustomerAccountsRequest request) {
        return statisticalService.getStatisticalCustomerAccounts(request);
    }

    @GetMapping("account/repairers")
    public ResponseEntity<StatisticalRepairerAccountsResponse> getStatisticalRepairerAccounts(StatisticalRepairerAccountsRequest request) {
        return statisticalService.getStatisticalRepairerAccounts(request);
    }

    @GetMapping("requests")
    public ResponseEntity<StatisticalRequestsResponse> getStatisticalRequests(StatisticalRequestsRequest request) {
        return statisticalService.getStatisticalRequests(request);
    }
}
