package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest extends DataRequest{
    private String oldPassword;
    private String newPassword;
}
