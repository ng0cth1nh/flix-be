package com.fpt.flix.flix_app.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.flix.flix_app.configurations.AppConf;
import com.fpt.flix.flix_app.models.db.Role;
import com.fpt.flix.flix_app.models.db.User;
import com.fpt.flix.flix_app.models.errors.GeneralException;
import com.fpt.flix.flix_app.models.requests.RegisterCustomerRequest;
import com.fpt.flix.flix_app.models.responses.RegisterCustomerResponse;
import com.fpt.flix.flix_app.models.responses.TokenResponse;
import com.fpt.flix.flix_app.repositories.UserRepository;
import com.fpt.flix.flix_app.services.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fpt.flix.flix_app.constants.Constant.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@RestController
@Slf4j
@RequestMapping("api/v1/")
public class CustomerController {
    private final CustomerService customerService;
    private final UserRepository userRepository;

    public CustomerController(CustomerService customerService,
                              UserRepository userRepository) {
        this.customerService = customerService;
        this.userRepository = userRepository;
    }

    @PostMapping("register/customer")
    public ResponseEntity<RegisterCustomerResponse> registerCustomer(@RequestBody RegisterCustomerRequest request) throws JsonProcessingException {
        return customerService.registerCustomer(request);
    }

    @GetMapping("/token/refresh")
    public void refreshToken(HttpServletRequest request,
                             HttpServletResponse response) throws IOException {
        customerService.refreshToken(request, response);
    }

}
