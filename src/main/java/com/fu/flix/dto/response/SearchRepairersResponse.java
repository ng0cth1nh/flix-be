package com.fu.flix.dto.response;

import com.fu.flix.dto.ISearchRepairersDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchRepairersResponse {
    private List<ISearchRepairersDTO> repairers;
}
