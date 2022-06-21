package com.fu.flix.dto.request;

import lombok.Data;

@Data
public class CommentRequest extends DataRequest {
    private String requestCode;
    private Integer rating;
    private String comment;
}
