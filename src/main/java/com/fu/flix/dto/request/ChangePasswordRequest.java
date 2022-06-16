package com.fu.flix.dto.request;

import lombok.Data;

@Data
public class ChangePasswordRequest extends DataRequest{
    private String oldPassword;
    private String newPassword;
}
