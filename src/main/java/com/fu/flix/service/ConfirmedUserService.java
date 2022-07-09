package com.fu.flix.service;

import com.fu.flix.dto.request.CommentRequest;
import com.fu.flix.dto.request.GetFixedServiceRequest;
import com.fu.flix.dto.request.GetInvoiceRequest;
import com.fu.flix.dto.response.CommentResponse;
import com.fu.flix.dto.response.GetFixedServiceResponse;
import com.fu.flix.dto.response.GetInvoiceResponse;
import org.springframework.http.ResponseEntity;

public interface ConfirmedUserService {
    ResponseEntity<CommentResponse> createComment(CommentRequest request);

    ResponseEntity<GetInvoiceResponse> getInvoice(GetInvoiceRequest request);

    ResponseEntity<GetFixedServiceResponse> getFixedServices(GetFixedServiceRequest request);

}
