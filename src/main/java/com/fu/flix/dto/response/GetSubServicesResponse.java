package com.fu.flix.dto.response;

import com.fu.flix.dto.AdminSubServiceDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetSubServicesResponse {
    private List<AdminSubServiceDTO> subServices;
}
