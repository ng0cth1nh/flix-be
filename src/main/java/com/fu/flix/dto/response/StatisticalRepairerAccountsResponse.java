package com.fu.flix.dto.response;

import com.fu.flix.dto.StatisticalRepairerAccountDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StatisticalRepairerAccountsResponse {
    private List<StatisticalRepairerAccountDTO> data;
}
