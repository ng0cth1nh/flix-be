package com.fu.flix.dto.response;

import com.fu.flix.dto.ITransactionDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchTransactionsResponse {
    private List<ITransactionDTO> transactions;
}
