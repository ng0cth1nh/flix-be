package com.fu.flix.dto.response;

import com.fu.flix.dto.PendingRepairerDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PendingRepairersResponse {
    private List<PendingRepairerDTO> repairerList;
}
