package com.fu.flix.service;

import com.fu.flix.dto.request.CreateFeedbackRequest;
import com.fu.flix.entity.Feedback;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FeedbackService {
    void validateCreateFeedbackInput(CreateFeedbackRequest request);

    void postFeedbackImages(Feedback feedback, List<MultipartFile> images) throws IOException;
}
