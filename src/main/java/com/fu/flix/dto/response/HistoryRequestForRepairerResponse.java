package com.fu.flix.dto.response;

import com.fu.flix.dto.HistoryRequestForRepairerDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class HistoryRequestForRepairerResponse {
    private List<HistoryRequestForRepairerDTO> requestHistories;
}
