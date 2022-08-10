package com.fu.flix.dto.response;

import com.fu.flix.dto.AccessoryOutputDTO;
import com.fu.flix.dto.ExtraServiceOutputDTO;
import com.fu.flix.dto.SubServiceOutputDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetFixedServiceResponse {
    private List<SubServiceOutputDTO> subServices;
    private List<AccessoryOutputDTO> accessories;
    private List<ExtraServiceOutputDTO> extraServices;
}
