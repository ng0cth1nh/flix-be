package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAdminProfileRequest extends DataRequest {
    private String fullName;
    private String email;
}
