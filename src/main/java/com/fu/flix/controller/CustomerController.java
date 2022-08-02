package com.fu.flix.controller;

import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@Slf4j
@RequestMapping("api/v1/customer")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("request/repair")
    public ResponseEntity<RequestingRepairResponse> createFixingRequest(@RequestBody RequestingRepairRequest request) throws IOException {
        return customerService.createFixingRequest(request);
    }

    @PostMapping("request/cancel")
    public ResponseEntity<CancelRequestForCustomerResponse> cancelFixingRequest(@RequestBody CancelRequestForCustomerRequest request) throws IOException {
        return customerService.cancelFixingRequest(request);
    }

    @GetMapping("request/histories")
    public ResponseEntity<HistoryRequestForCustomerResponse> getFixingRequestHistories(HistoryRequestForCustomerRequest request) {
        return customerService.getFixingRequestHistories(request);
    }

    @GetMapping("request/detail")
    public ResponseEntity<RequestingDetailForCustomerResponse> getDetailFixingRequest(RequestingDetailForCustomerRequest request) {
        return customerService.getDetailFixingRequest(request);
    }

    @GetMapping("address/list")
    public ResponseEntity<UserAddressResponse> getListAddress(UserAddressRequest request) {
        return customerService.getCustomerAddresses(request);
    }

    @DeleteMapping("address")
    public ResponseEntity<DeleteAddressResponse> deleteCustomerAddress(DeleteAddressRequest request) {
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
    public ResponseEntity<RepairerResponse> getRepairerProfile(RepairerRequest request) {
        return customerService.getRepairerProfile(request);
    }

    @GetMapping("repairer/comment")
    public ResponseEntity<RepairerCommentResponse> getRepairerProfile(RepairerCommentRequest request) {
        return customerService.getRepairerComments(request);
    }

    @PutMapping("address/main")
    public ResponseEntity<ChooseMainAddressResponse> chooseMainAddress(@RequestBody ChooseMainAddressRequest request) {
        return customerService.chooseMainAddress(request);
    }
}
