package com.fu.flix.controller;

import com.fu.flix.dto.request.CancelRequestForRepairerRequest;
import com.fu.flix.dto.request.RepairerApproveRequest;
import com.fu.flix.dto.request.RequestingDetailForRepairerRequest;
import com.fu.flix.dto.response.CancelRequestForRepairerResponse;
import com.fu.flix.dto.response.RepairerApproveResponse;
import com.fu.flix.dto.response.RequestingDetailForRepairerResponse;
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
}
