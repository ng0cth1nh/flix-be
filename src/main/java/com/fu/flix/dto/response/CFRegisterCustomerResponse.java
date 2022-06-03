package com.fu.flix.dto.response;

import lombok.Data;

@Data
public class CFRegisterCustomerResponse {
    private String accessToken;
    private String refreshToken;
}
