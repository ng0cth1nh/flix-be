package com.fu.flix.dto.response;

import lombok.Data;

@Data
public class CFRegisterResponse {
    private String accessToken;
    private String refreshToken;
}
