package com.fu.flix.dto.response;

import com.fu.flix.dto.AccessoryOutputDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchAccessoriesResponse {
    private List<AccessoryOutputDTO> accessories;
}
