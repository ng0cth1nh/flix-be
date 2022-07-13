package com.fu.flix.dto.response;

import com.fu.flix.dto.AdminSearchServiceDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminSearchServicesResponse {
    private List<AdminSearchServiceDTO> services;
}
