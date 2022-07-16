package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BanUserRequest extends DataRequest {
    private String phone;
    private String banReason;
}
