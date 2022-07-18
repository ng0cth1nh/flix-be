package com.fu.flix.service;

import com.fu.flix.dto.request.CreateFeedbackRequest;
import com.fu.flix.entity.*;

public interface ValidatorService {
    User getUserValidated(String username);

    User getUserValidated(Long userId);

    Integer getPageSize(Integer pageSize);

    Integer getPageNumber(Integer pageNumber);

    Service getServiceValidated(Long serviceId);

    Category getCategoryValidated(Long categoryId);

    SubService getSubServiceValidated(Long subServiceId);

    void validateCreateFeedbackInput(CreateFeedbackRequest request);

    Accessory getAccessoryValidated(Long accessoryId);

    Feedback getFeedbackValidated(Long feedbackId);
}
