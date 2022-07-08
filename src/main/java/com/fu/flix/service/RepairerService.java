package com.fu.flix.service;

import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import org.springframework.http.ResponseEntity;

public interface RepairerService {
    ResponseEntity<RepairerApproveResponse> approveRequest(RepairerApproveRequest request);

    ResponseEntity<RequestingDetailForRepairerResponse> getRepairRequestDetail(RequestingDetailForRepairerRequest request);

    ResponseEntity<CancelRequestForRepairerResponse> cancelFixingRequest(CancelRequestForRepairerRequest request);

    ResponseEntity<HistoryRequestForRepairerResponse> getFixingRequestHistories(HistoryRequestForRepairerRequest request);

    ResponseEntity<CreateInvoiceResponse> createInvoice(CreateInvoiceRequest request);

    ResponseEntity<ConfirmInvoicePaidResponse> confirmInvoicePaid(ConfirmInvoicePaidRequest request);

    ResponseEntity<ConfirmFixingResponse> confirmFixing(ConfirmFixingRequest request);

    ResponseEntity<RequestingSuggestionResponse> getSuggestionRequestList(RequestingSuggestionRequest request);

    ResponseEntity<RequestingFilterResponse> getFilterRequestList(RequestingFilterRequest request);

    ResponseEntity<AddSubServicesToInvoiceResponse> putSubServicesToInvoice(AddSubServicesToInvoiceRequest request);

    ResponseEntity<AddAccessoriesToInvoiceResponse> putAccessoriesToInvoice(AddAccessoriesToInvoiceRequest request);

    ResponseEntity<AddExtraServiceToInvoiceResponse> putExtraServiceToInvoice(AddExtraServiceToInvoiceRequest request);

    ResponseEntity<SearchSubServicesResponse> searchSubServicesByService(SearchSubServicesRequest request);
    ResponseEntity<SearchAccessoriesResponse> searchAccessoriesByService(SearchAccessoriesRequest request);

}
