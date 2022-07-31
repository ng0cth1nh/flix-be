package com.fu.flix.dto.response;

import com.fu.flix.dto.RepairerTransactionDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RepairerTransactionsResponse {
    private List<RepairerTransactionDTO> transactions;
}
