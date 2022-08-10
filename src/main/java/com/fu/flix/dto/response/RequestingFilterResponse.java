package com.fu.flix.dto.response;

import com.fu.flix.dto.RequestingDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RequestingFilterResponse {
    private List<RequestingDTO> requestList;
}
