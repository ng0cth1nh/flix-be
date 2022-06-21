package com.fu.flix.controller;

import com.fu.flix.dto.request.CommentRequest;
import com.fu.flix.dto.response.CommentResponse;
import com.fu.flix.service.ConfirmedUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("api/v1/confirmedUser")
public class ConfirmedUserController {
    private final ConfirmedUserService confirmedUserService;

    public ConfirmedUserController(ConfirmedUserService confirmedUserService) {
        this.confirmedUserService = confirmedUserService;
    }

    @PostMapping("comment")
    public ResponseEntity<CommentResponse> createComment(@RequestBody CommentRequest request) {
        return confirmedUserService.createComment(request);
    }
}
