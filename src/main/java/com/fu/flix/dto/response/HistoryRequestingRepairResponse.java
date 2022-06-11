package com.fu.flix.dto.response;

import com.fu.flix.dto.HistoryRepairRequestDTO;
import lombok.Data;

import java.util.List;

@Data
public class HistoryRequestingRepairResponse {
    private List<HistoryRepairRequestDTO> requestHistories;
}
