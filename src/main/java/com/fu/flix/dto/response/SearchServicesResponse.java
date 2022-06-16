package com.fu.flix.dto.response;

import com.fu.flix.dto.SearchServiceDTO;
import lombok.Data;

import java.util.List;

@Data
public class SearchServicesResponse {
    private List<SearchServiceDTO> services;
}
