package com.fu.flix.dto.request;

import com.fu.flix.dto.ServiceDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ServiceResponse {
    private List<ServiceDTO> services;
}
