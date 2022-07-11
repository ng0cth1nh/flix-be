package com.fu.flix.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CFRegisterRepairerResponse {
    private String accessToken;
    private String refreshToken;
    private String message;
}
