package com.fu.flix.dto.response;

import com.fu.flix.dto.StatisticalRequestDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StatisticalRequestsResponse {
    private List<StatisticalRequestDTO> data;
}
