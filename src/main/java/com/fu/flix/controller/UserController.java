package com.fu.flix.controller;


import com.fu.flix.dto.request.MainAddressRequest;
import com.fu.flix.dto.response.MainAddressResponse;
import com.fu.flix.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@Slf4j
@RequestMapping("api/v1/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("address/main")
    public ResponseEntity<MainAddressResponse> getMainAddress(MainAddressRequest request) {
        return userService.getMainAddress(request);
    }
}
