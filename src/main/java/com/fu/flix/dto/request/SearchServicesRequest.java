package com.fu.flix.dto.request;

import lombok.Data;

@Data
public class SearchServicesRequest extends DataRequest{
    private String keyword;
}
