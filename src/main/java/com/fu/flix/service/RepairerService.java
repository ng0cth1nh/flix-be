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
}
