package com.fu.flix.dto.response;

import com.fu.flix.dto.CategoryDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetCategoriesResponse {
    private List<CategoryDTO> categories;
}
