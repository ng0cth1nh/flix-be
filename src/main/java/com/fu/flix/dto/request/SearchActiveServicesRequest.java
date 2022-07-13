package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchActiveServicesRequest extends DataRequest{
    private String keyword;
}
