package com.fu.flix.service;

import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface AdminService {
    ResponseEntity<GetAdminProfileResponse> getAdminProfile(GetAdminProfileRequest request);

    ResponseEntity<UpdateAdminProfileResponse> updateAdminProfile(UpdateAdminProfileRequest request);

    ResponseEntity<GetCategoriesResponse> getCategories(GetCategoriesRequest request);

    ResponseEntity<CreateCategoryResponse> createCategory(CreateCategoryRequest request) throws IOException;

    ResponseEntity<UpdateCategoryResponse> updateCategory(UpdateCategoryRequest request) throws IOException;
}
