package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCreateFeedBackRequest extends CreateFeedbackRequest {
    private String phone;
}
