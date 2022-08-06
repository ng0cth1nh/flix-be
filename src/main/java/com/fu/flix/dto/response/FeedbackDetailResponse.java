package com.fu.flix.dto.response;

import com.fu.flix.dto.FeedbackResponseDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FeedbackDetailResponse {
    private String phone;
    private Long handleByAdminId;
    private Long createdById;
    private Long userId;
    private String feedbackType;
    private String requestCode;
    private String title;
    private String description;
    private List<String> images;
    private String status;
    private List<FeedbackResponseDTO> responses;
    private String createdAt;
    private String updatedAt;
}
