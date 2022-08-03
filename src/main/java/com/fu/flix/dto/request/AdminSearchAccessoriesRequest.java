package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminSearchAccessoriesRequest extends DataRequest {
    private String keyword;
}
