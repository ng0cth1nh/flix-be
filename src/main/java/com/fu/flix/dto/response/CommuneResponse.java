package com.fu.flix.dto.response;

import com.fu.flix.dto.CommuneDTO;
import lombok.Data;

import java.util.List;

@Data
public class CommuneResponse {
    private List<CommuneDTO> communes;
}
