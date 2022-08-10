package com.fu.flix.dto.response;

import com.fu.flix.dto.ServiceInfoDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetAllServicesResponse {
    private List<ServiceInfoDTO> services;
}
