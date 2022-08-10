package com.fu.flix.dto.response;

import com.fu.flix.dto.CategoryInfoDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetAllCategoriesResponse {
    private List<CategoryInfoDTO> categories;
}
