package com.fu.flix.dto.response;

import com.fu.flix.dto.ISearchCustomerDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchCustomerResponse {
    private List<ISearchCustomerDTO> customers;
}
