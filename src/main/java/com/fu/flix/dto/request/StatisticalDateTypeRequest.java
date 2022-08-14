package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatisticalDateTypeRequest extends DataRequest {
    private String from;
    private String to;
    String type;
}
