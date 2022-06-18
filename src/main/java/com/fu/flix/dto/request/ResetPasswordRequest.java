package com.fu.flix.dto.request;

import lombok.Data;

@Data
public class ResetPasswordRequest extends DataRequest {
    private String newPassword;
}
