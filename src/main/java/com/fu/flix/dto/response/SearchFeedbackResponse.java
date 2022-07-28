package com.fu.flix.dto.response;

import com.fu.flix.dto.ISearchFeedbackDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchFeedbackResponse {
    private List<ISearchFeedbackDTO> feedbackList;
}
