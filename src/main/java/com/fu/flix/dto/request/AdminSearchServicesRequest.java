package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminSearchServicesRequest extends DataRequest {
    private String keyword;
    private Long categoryId;
}
