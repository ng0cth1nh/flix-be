package com.fu.flix.dto.response;

import com.fu.flix.dto.SearchServiceDTO;
import lombok.Data;

import java.util.List;

@Data
public class SearchActiveServicesResponse {
    private List<SearchServiceDTO> services;
}
