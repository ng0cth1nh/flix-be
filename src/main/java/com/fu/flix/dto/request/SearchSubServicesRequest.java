package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchSubServicesRequest extends DataRequest {
    private String keyword;
    private Long serviceId;
}
