package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestingSuggestionRequest extends DataRequest{
    private String type;
}
