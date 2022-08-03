package com.fu.flix.dto.response;

import com.fu.flix.dto.ServiceDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetServicesResponse {
    private List<ServiceDTO> services;
    private long totalRecord;
}
