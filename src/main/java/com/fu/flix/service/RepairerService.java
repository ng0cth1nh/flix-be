package com.fu.flix.service;

import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.entity.Balance;
import org.springframework.http.ResponseEntity;

public interface RepairerService {
    ResponseEntity<RepairerApproveResponse> approveRequest(RepairerApproveRequest request);

    ResponseEntity<RequestingDetailForRepairerResponse> getRepairRequestDetail(RequestingDetailForRepairerRequest request);

    ResponseEntity<CancelRequestForRepairerResponse> cancelFixingRequest(CancelRequestForRepairerRequest request);

    ResponseEntity<HistoryRequestForRepairerResponse> getFixingRequestHistories(HistoryRequestForRepairerRequest request);

    ResponseEntity<CreateInvoiceResponse> createInvoice(CreateInvoiceRequest request);

    ResponseEntity<ConfirmInvoicePaidResponse> confirmInvoicePaid(ConfirmInvoicePaidRequest request);

    ResponseEntity<ConfirmFixingResponse> confirmFixing(ConfirmFixingRequest request);

    ResponseEntity<AddSubServicesToInvoiceResponse> putSubServicesToInvoice(AddSubServicesToInvoiceRequest request);

    ResponseEntity<AddAccessoriesToInvoiceResponse> putAccessoriesToInvoice(AddAccessoriesToInvoiceRequest request);

    ResponseEntity<AddExtraServiceToInvoiceResponse> putExtraServicesToInvoice(AddExtraServiceToInvoiceRequest request);

    ResponseEntity<RepairerWithdrawResponse> requestWithdraw(RepairerWithdrawRequest request);

    ResponseEntity<RepairerTransactionsResponse> getTransactionHistories(RepairerTransactionsRequest request);
}
