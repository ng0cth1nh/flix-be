package com.fu.flix.dto.response;

import com.fu.flix.dto.IRepairerDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetRepairersResponse {
    private List<IRepairerDTO> repairerList;
    private long totalRecord;
}
