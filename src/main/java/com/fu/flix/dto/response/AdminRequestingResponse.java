package com.fu.flix.dto.response;

import com.fu.flix.dto.AdminRequestingDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminRequestingResponse {
    private List<AdminRequestingDTO> requestList;
    private long totalRecord;
}
