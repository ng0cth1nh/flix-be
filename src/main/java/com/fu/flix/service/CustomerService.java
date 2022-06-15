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

    ResponseEntity<UserAddressResponse> getUserAddresses(UserAddressRequest request);

    ResponseEntity<DeleteAddressResponse> deleteUserAddress(DeleteAddressRequest request);

    ResponseEntity<EditAddressResponse> editUserAddress(EditAddressRequest request);
}
