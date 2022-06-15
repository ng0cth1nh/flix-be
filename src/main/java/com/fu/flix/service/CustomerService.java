package com.fu.flix.service;

import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import org.springframework.http.ResponseEntity;

public interface CustomerService {
    ResponseEntity<RequestingRepairResponse> createFixingRequest(RequestingRepairRequest request);

    ResponseEntity<CancelRequestingRepairResponse> cancelFixingRequest(CancelRequestingRepairRequest request);

    ResponseEntity<HistoryRequestingRepairResponse> getFixingRequestHistories(HistoryRequestingRepairRequest request);

    ResponseEntity<DetailRequestingRepairResponse> getDetailFixingRequest(DetailRequestingRepairRequest request);

    ResponseEntity<MainAddressResponse> getMainAddress(MainAddressRequest request);

    ResponseEntity<UserAddressResponse> getCustomerAddresses(UserAddressRequest request);

    ResponseEntity<DeleteAddressResponse> deleteCustomerAddress(DeleteAddressRequest request);

    ResponseEntity<EditAddressResponse> editCustomerAddress(EditAddressRequest request);

    ResponseEntity<CreateAddressResponse> createCustomerAddress(CreateAddressRequest request);

    ResponseEntity<CustomerProfileResponse> getCustomerProfile(CustomerProfileRequest request);

    ResponseEntity<UpdateCustomerProfileResponse> updateCustomerProfile(UpdateCustomerProfileRequest request);
}
