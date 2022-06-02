package com.fpt.flix.flix_app.models.responses;

import lombok.Data;

@Data
public class CFRegisterCustomerResponse {
    private String accessToken;
    private String refreshToken;
}
