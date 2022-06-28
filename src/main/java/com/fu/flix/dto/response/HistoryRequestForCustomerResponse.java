package com.fu.flix.dto.response;

import com.fu.flix.dto.HistoryRequestForCustomerDTO;
import lombok.Data;

import java.util.List;

@Data
public class HistoryRequestForCustomerResponse {
    private List<HistoryRequestForCustomerDTO> requestHistories;
}
