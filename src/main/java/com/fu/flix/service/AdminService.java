package com.fu.flix.service;

import com.fu.flix.dto.request.CreateCategoryRequest;
import com.fu.flix.dto.request.GetAdminProfileRequest;
import com.fu.flix.dto.request.GetCategoriesRequest;
import com.fu.flix.dto.request.UpdateAdminProfileRequest;
import com.fu.flix.dto.response.CreateCategoryResponse;
import com.fu.flix.dto.response.GetAdminProfileResponse;
import com.fu.flix.dto.response.GetCategoriesResponse;
import com.fu.flix.dto.response.UpdateAdminProfileResponse;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface AdminService {
    ResponseEntity<GetAdminProfileResponse> getAdminProfile(GetAdminProfileRequest request);

    ResponseEntity<UpdateAdminProfileResponse> updateAdminProfile(UpdateAdminProfileRequest request);

    ResponseEntity<GetCategoriesResponse> getCategories(GetCategoriesRequest request);

    ResponseEntity<CreateCategoryResponse> createCategory(CreateCategoryRequest request) throws IOException;
}
