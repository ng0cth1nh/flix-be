package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchWithdrawRequest extends DataRequest {
    private String keyword;
    private String withdrawType;
}
