package com.fu.flix.dto.response;

import com.fu.flix.dto.StatisticalTransactionDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StatisticalTransactionsResponse {
    private List<StatisticalTransactionDTO> data;
}
