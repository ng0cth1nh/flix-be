package com.fu.flix.controller;

import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("api/v1/customer")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("request/repair")
    public ResponseEntity<RequestingRepairResponse> createFixingRequest(@RequestBody RequestingRepairRequest request) {
        return customerService.createFixingRequest(request);
    }

    @PostMapping("request/cancel")
    public ResponseEntity<CancelRequestingRepairResponse> cancelFixingRequest(@RequestBody CancelRequestingRepairRequest request) {
        return customerService.cancelFixingRequest(request);
    }

    @GetMapping("request/histories")
    public ResponseEntity<HistoryRequestingRepairResponse> getFixingRequestHistories(HistoryRequestingRepairRequest request) {
        return customerService.getFixingRequestHistories(request);
    }

    @GetMapping("request/detail")
    public ResponseEntity<DetailRequestingRepairResponse> getDetailFixingRequest(DetailRequestingRepairRequest request) {
        return customerService.getDetailFixingRequest(request);
    }

    @GetMapping("address/list")
    public ResponseEntity<UserAddressResponse> getListAddress(UserAddressRequest request) {
        return customerService.getCustomerAddresses(request);
    }

    @DeleteMapping("address")
    public ResponseEntity<DeleteAddressResponse> deleteCustomerAddress(@RequestBody DeleteAddressRequest request) {
        return customerService.deleteCustomerAddress(request);
    }

    @PutMapping("address")
    public ResponseEntity<EditAddressResponse> editCustomerAddress(@RequestBody EditAddressRequest request) {
        return customerService.editCustomerAddress(request);
    }

    @GetMapping("address/main")
    public ResponseEntity<MainAddressResponse> getMainAddress(MainAddressRequest request) {
        return customerService.getMainAddress(request);
    }

    @PostMapping("address")
    public ResponseEntity<CreateAddressResponse> editCustomerAddress(@RequestBody CreateAddressRequest request) {
        return customerService.createCustomerAddress(request);
    }

    @GetMapping("profile")
    public ResponseEntity<CustomerProfileResponse> getCustomerProfile(CustomerProfileRequest request) {
        return customerService.getCustomerProfile(request);
    }

    @PutMapping("profile")
    public ResponseEntity<UpdateCustomerProfileResponse> updateCustomerProfile(@RequestBody UpdateCustomerProfileRequest request) {
        return customerService.updateCustomerProfile(request);
    }

    @GetMapping("repairer/profile")
    public ResponseEntity<RepairerProfileResponse> getRepairerProfile(RepairerProfileRequest request) {
        return customerService.getRepairerProfile(request);
    }

    @GetMapping("repairer/comment")
    public ResponseEntity<RepairerCommentResponse> getRepairerProfile(RepairerCommentRequest request) {
        return customerService.getRepairerComments(request);
    }
}
