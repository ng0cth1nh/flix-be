package com.fu.flix.service;

import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.entity.Feedback;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AdminService {
    ResponseEntity<GetAdminProfileResponse> getAdminProfile(GetAdminProfileRequest request);

    ResponseEntity<UpdateAdminProfileResponse> updateAdminProfile(UpdateAdminProfileRequest request);

    ResponseEntity<GetCategoriesResponse> getCategories(GetCategoriesRequest request);

    ResponseEntity<CreateCategoryResponse> createCategory(CreateCategoryRequest request) throws IOException;

    ResponseEntity<UpdateCategoryResponse> updateCategory(UpdateCategoryRequest request) throws IOException;

    ResponseEntity<GetServicesResponse> getServices(GetServicesRequest request);

    ResponseEntity<CreateServiceResponse> createService(CreateServiceRequest request) throws IOException;

    ResponseEntity<UpdateServiceResponse> updateService(UpdateServiceRequest request) throws IOException;

    ResponseEntity<AdminSearchServicesResponse> searchServices(AdminSearchServicesRequest request);

    ResponseEntity<GetSubServicesResponse> getSubServices(GetSubServicesRequest request);

    ResponseEntity<CreateSubServiceResponse> createSubService(CreateSubServiceRequest request);

    ResponseEntity<UpdateSubServiceResponse> updateSubService(UpdateSubServiceRequest request);

    ResponseEntity<AdminRequestingResponse> getRequests(AdminRequestingRequest request);

    ResponseEntity<GetCustomersResponse> getCustomers(GetCustomersRequest request);

    ResponseEntity<GetRepairersResponse> getRepairers(GetRepairersRequest request);

    ResponseEntity<GetCustomerDetailResponse> getCustomerDetail(GetCustomerDetailRequest request);

    ResponseEntity<GetBanUsersResponse> getBanUsers(GetBanUsersRequest request);

    ResponseEntity<BanUserResponse> banUser(BanUserRequest request);

    ResponseEntity<AdminCreateFeedBackResponse> createFeedback(AdminCreateFeedBackRequest request) throws IOException;

    ResponseEntity<FeedbackDetailResponse> getFeedbackDetail(FeedbackDetailRequest request);

    ResponseEntity<AdminGetAccessoriesResponse> getAccessories(AdminGetAccessoriesRequest request);

    ResponseEntity<PendingRepairersResponse> getPendingRepairers(PendingRepairersRequest request);

    ResponseEntity<CreateAccessoryResponse> createAccessory(CreateAccessoryRequest request);
}
