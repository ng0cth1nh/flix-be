package com.fu.flix.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class FeedbackRequest extends DataRequest {
    private String feedbackType;
    private String requestCode;
    private String title;
    private String description;
    private List<MultipartFile> images;
}
