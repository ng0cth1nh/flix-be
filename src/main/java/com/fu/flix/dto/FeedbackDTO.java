package com.fu.flix.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackDTO {
    private Long id;
    private String phone;
    private String feedbackType;
    private String title;
    private String createdAt;
    private String status;
}
