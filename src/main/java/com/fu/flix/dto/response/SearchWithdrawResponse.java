package com.fu.flix.dto.response;

import com.fu.flix.dto.ISearchWithdrawDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchWithdrawResponse {
    private List<ISearchWithdrawDTO> withdrawList;
}
