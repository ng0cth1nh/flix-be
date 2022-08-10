package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminGetAccessoriesRequest {
    private Integer pageSize;
    private Integer pageNumber;
}
