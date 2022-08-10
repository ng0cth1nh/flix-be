package com.fu.flix.dto.response;

import com.fu.flix.dto.FeedbackDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FeedbacksResponse {
    private List<FeedbackDTO> feedbackList;
    private long totalRecord;
}
