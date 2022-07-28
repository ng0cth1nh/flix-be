package com.fu.flix.dto.response;

import com.fu.flix.dto.ICategoryDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchCategoriesResponse {
    List<ICategoryDTO> categories;
}
