package com.fu.flix.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmFixingResponse {
    private String requestCode;
    private String status;
    private String message;
}
