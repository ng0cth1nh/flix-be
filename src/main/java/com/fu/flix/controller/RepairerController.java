package com.fu.flix.controller;

import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.service.RepairerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("api/v1/repairer")
public class RepairerController {
    private final RepairerService repairerService;

    public RepairerController(RepairerService repairerService) {
        this.repairerService = repairerService;
    }

    @PostMapping("request/approve")
    public ResponseEntity<RepairerApproveResponse> approvalRequest(@RequestBody RepairerApproveRequest request) {
        return repairerService.approveRequest(request);
    }

    @GetMapping("request/detail")
    public ResponseEntity<RequestingDetailForRepairerResponse> getRepairRequestDetail(RequestingDetailForRepairerRequest request) {
        return repairerService.getRepairRequestDetail(request);
    }

    @PostMapping("request/cancel")
    public ResponseEntity<CancelRequestForRepairerResponse> cancelFixingRequest(@RequestBody CancelRequestForRepairerRequest request) {
        return repairerService.cancelFixingRequest(request);
    }

    @GetMapping("request/histories")
    public ResponseEntity<HistoryRequestForRepairerResponse> getFixingRequestHistories(HistoryRequestForRepairerRequest request) {
        return repairerService.getFixingRequestHistories(request);
    }

    @PostMapping("invoice")
    public ResponseEntity<CreateInvoiceResponse> createInvoice(@RequestBody CreateInvoiceRequest request) {
        return repairerService.createInvoice(request);
    }

    @PutMapping("invoice/confirm/paid")
    public ResponseEntity<ConfirmInvoicePaidResponse> confirmInvoicePaid(@RequestBody ConfirmInvoicePaidRequest request) {
        return repairerService.confirmInvoicePaid(request);
    }

    @PutMapping("request/confirmFixing")
    public ResponseEntity<ConfirmFixingResponse> confirmFixing(@RequestBody ConfirmFixingRequest request) {
        return repairerService.confirmFixing(request);
    }

    @GetMapping("request/list/suggestion")
    public ResponseEntity<RepairerSuggestionResponse> getSuggestionRequestList(RepairerSuggestionRequest request) {
        return repairerService.getSuggestionRequestList(request);
    }
}
