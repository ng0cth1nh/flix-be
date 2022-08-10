package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class CreateFeedbackRequest extends DataRequest {
    private String feedbackType;
    private String requestCode;
    private String title;
    private String description;
    private List<MultipartFile> images;
}
