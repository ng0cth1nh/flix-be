package com.fu.flix.dto.response;

import com.fu.flix.dto.CityDTO;
import lombok.Data;

import java.util.List;

@Data
public class CityResponse {
    private List<CityDTO> cities;
}
