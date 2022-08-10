package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest extends DataRequest {
    private String requestCode;
    private Integer rating;
    private String comment;
}
