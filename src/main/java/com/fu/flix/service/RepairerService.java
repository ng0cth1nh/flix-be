package com.fu.flix.service;

import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.entity.Balance;
import com.fu.flix.entity.Invoice;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface RepairerService {
    ResponseEntity<RepairerApproveResponse> approveRequest(RepairerApproveRequest request) throws IOException;

    ResponseEntity<RequestingDetailForRepairerResponse> getRepairRequestDetail(RequestingDetailForRepairerRequest request);

    ResponseEntity<CancelRequestForRepairerResponse> cancelFixingRequest(CancelRequestForRepairerRequest request) throws IOException;

    ResponseEntity<HistoryRequestForRepairerResponse> getFixingRequestHistories(HistoryRequestForRepairerRequest request);

    ResponseEntity<CreateInvoiceResponse> createInvoice(CreateInvoiceRequest request) throws IOException;

    ResponseEntity<ConfirmInvoicePaidResponse> confirmInvoicePaid(ConfirmInvoicePaidRequest request) throws IOException;

    ResponseEntity<ConfirmFixingResponse> confirmFixing(ConfirmFixingRequest request) throws IOException;

    ResponseEntity<AddSubServicesToInvoiceResponse> putSubServicesToInvoice(AddSubServicesToInvoiceRequest request);

    ResponseEntity<AddAccessoriesToInvoiceResponse> putAccessoriesToInvoice(AddAccessoriesToInvoiceRequest request);

    ResponseEntity<AddExtraServiceToInvoiceResponse> putExtraServicesToInvoice(AddExtraServiceToInvoiceRequest request);

    ResponseEntity<RepairerWithdrawResponse> requestWithdraw(RepairerWithdrawRequest request);

    ResponseEntity<RepairerTransactionsResponse> getTransactionHistories(RepairerTransactionsRequest request);
    Long getCommission(Invoice invoice);
}
