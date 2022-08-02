package com.fu.flix.dto.response;

import com.fu.flix.dto.SubServiceOutputDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminSearchSubServicesResponse {
    private List<SubServiceOutputDTO> subServices;
}
