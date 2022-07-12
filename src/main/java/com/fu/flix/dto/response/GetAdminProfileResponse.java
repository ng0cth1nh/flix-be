package com.fu.flix.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAdminProfileResponse {
    private String fullName;
    private String phone;
    private String email;
}
