package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchCustomerRequest extends DataRequest {
    private String keyword;
}
