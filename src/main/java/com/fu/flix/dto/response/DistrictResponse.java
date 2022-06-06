package com.fu.flix.dto.response;

import com.fu.flix.dto.DistrictDTO;
import lombok.Data;

import java.util.List;

@Data
public class DistrictResponse {
    private List<DistrictDTO> districts;
}
