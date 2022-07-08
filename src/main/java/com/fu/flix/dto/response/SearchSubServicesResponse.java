package com.fu.flix.dto.response;

import com.fu.flix.dto.SubServiceDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchSubServicesResponse {
    private List<SubServiceDTO> subServices;
}
