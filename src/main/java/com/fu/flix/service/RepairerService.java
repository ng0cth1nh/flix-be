package com.fu.flix.service;

import com.fu.flix.dto.request.RepairerApproveRequest;
import com.fu.flix.dto.request.RequestingDetailRequest;
import com.fu.flix.dto.response.RepairerApproveResponse;
import com.fu.flix.dto.response.RequestingDetailResponse;
import org.springframework.http.ResponseEntity;

public interface RepairerService {
    ResponseEntity<RepairerApproveResponse> approveRequest(RepairerApproveRequest request);

    ResponseEntity<RequestingDetailResponse> getRepairRequestDetail(RequestingDetailRequest request);
}
