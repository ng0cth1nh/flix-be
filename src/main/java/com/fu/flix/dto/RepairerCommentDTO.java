package com.fu.flix.dto;

import lombok.Data;

@Data
public class RepairerCommentDTO {
    private Long customerId;
    private String customerName;
    private Integer rating;
    private String comment;
}
