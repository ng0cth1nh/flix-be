package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchRepairersRequest extends DataRequest {
    private String keyword;
    private String accountState;
    private String cvStatus;
}
