package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepairerTransactionsRequest extends DataRequest {
    private Integer pageNumber;
    private Integer pageSize;
}
