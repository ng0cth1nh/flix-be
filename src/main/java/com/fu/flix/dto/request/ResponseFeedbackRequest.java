package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseFeedbackRequest extends DataRequest {
    private Long id;
    private String status;
    private String response;
}
