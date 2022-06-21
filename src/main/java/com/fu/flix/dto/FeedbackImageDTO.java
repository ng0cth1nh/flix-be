package com.fu.flix.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FeedbackImageDTO {
    private MultipartFile image;
}
