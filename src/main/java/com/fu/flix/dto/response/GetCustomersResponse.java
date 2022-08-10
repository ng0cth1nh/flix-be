package com.fu.flix.dto.response;

import com.fu.flix.dto.ICustomerDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetCustomersResponse {
    private List<ICustomerDTO> customerList;
    private long totalRecord;
}
