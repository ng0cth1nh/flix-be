package com.fu.flix.service;

import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.entity.RepairRequest;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface CustomerService {
    ResponseEntity<RequestingRepairResponse> createFixingRequest(RequestingRepairRequest request);

    ResponseEntity<CancelRequestForCustomerResponse> cancelFixingRequest(CancelRequestForCustomerRequest request) throws IOException;

    ResponseEntity<HistoryRequestForCustomerResponse> getFixingRequestHistories(HistoryRequestForCustomerRequest request);

    ResponseEntity<RequestingDetailForCustomerResponse> getDetailFixingRequest(RequestingDetailForCustomerRequest request);

    ResponseEntity<MainAddressResponse> getMainAddress(MainAddressRequest request);

    ResponseEntity<UserAddressResponse> getCustomerAddresses(UserAddressRequest request);

    ResponseEntity<DeleteAddressResponse> deleteCustomerAddress(DeleteAddressRequest request);

    ResponseEntity<EditAddressResponse> editCustomerAddress(EditAddressRequest request);

    ResponseEntity<CreateAddressResponse> createCustomerAddress(CreateAddressRequest request);

    ResponseEntity<CustomerProfileResponse> getCustomerProfile(CustomerProfileRequest request);

    ResponseEntity<UpdateCustomerProfileResponse> updateCustomerProfile(UpdateCustomerProfileRequest request);

    ResponseEntity<RepairerResponse> getRepairerProfile(RepairerRequest request);

    ResponseEntity<RepairerCommentResponse> getRepairerComments(RepairerCommentRequest request);

    void updateRepairerAfterCancelRequest(String requestCode);
    void refundVoucher(RepairRequest repairRequest);
    ResponseEntity<ChooseMainAddressResponse> chooseMainAddress(ChooseMainAddressRequest request);
}
