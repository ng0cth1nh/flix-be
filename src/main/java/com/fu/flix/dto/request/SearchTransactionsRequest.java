package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchTransactionsRequest extends DataRequest {
    private String keyword;
    private String transactionType;
}
