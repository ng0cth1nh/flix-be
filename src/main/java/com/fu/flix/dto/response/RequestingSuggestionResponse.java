package com.fu.flix.dto.response;

import com.fu.flix.dto.RequestingDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class RequestingSuggestionResponse {
    private List<RequestingDTO> requestList;
}
