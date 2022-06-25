package com.fu.flix.service;

import com.fu.flix.dto.request.RepairerApproveRequest;
import com.fu.flix.dto.response.RepairerApproveResponse;
import org.springframework.http.ResponseEntity;

public interface RepairerService {
    ResponseEntity<RepairerApproveResponse> approveRequest(RepairerApproveRequest request);
}
