package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchCategoriesRequest extends DataRequest {
    private String keyword;
}
