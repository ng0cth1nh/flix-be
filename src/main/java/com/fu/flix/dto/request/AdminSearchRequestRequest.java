package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminSearchRequestRequest {
    private String keyword;
    private String status;
}
