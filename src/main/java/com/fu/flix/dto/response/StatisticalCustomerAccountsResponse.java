package com.fu.flix.dto.response;

import com.fu.flix.dto.StatisticalCustomerAccountDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StatisticalCustomerAccountsResponse {
    private List<StatisticalCustomerAccountDTO> data;
}
