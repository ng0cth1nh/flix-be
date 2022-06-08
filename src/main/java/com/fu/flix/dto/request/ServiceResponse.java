package com.fu.flix.dto.request;

import com.fu.flix.dto.ServiceDTO;
import lombok.Data;

import java.util.List;

@Data
public class ServiceResponse {
    private List<ServiceDTO> services;
}
