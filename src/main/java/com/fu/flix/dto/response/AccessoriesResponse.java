package com.fu.flix.dto.response;

import com.fu.flix.dto.AccessoryOutputDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AccessoriesResponse {
    private List<AccessoryOutputDTO> accessories;
}
