package com.fu.flix.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FeedbackDetailResponse {
    private String phone;
    private String feedbackType;
    private String requestCode;
    private String title;
    private String description;
    private List<String> images;
    private String status;
    private String response;
    private String createdAt;
    private String updatedAt;
}
