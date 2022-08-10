package com.fu.flix.dto.response;

import com.fu.flix.dto.IWithdrawHistoryDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WithdrawHistoriesResponse {
    private List<IWithdrawHistoryDTO> withdrawList;
    private long totalRecord;
}
