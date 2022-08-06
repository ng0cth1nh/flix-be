package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnbanUserRequest extends DataRequest {
    private String phone;
}
