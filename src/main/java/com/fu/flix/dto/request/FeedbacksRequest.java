package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbacksRequest extends DataRequest {
    private Integer pageNumber;
    private Integer pageSize;
}
