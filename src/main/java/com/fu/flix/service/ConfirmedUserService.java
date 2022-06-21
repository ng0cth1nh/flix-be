package com.fu.flix.service;

import com.fu.flix.dto.request.CommentRequest;
import com.fu.flix.dto.response.CommentResponse;
import org.springframework.http.ResponseEntity;

public interface ConfirmedUserService {
    ResponseEntity<CommentResponse> createComment(CommentRequest request);
}
