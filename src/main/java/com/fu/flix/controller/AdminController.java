package com.fu.flix.controller;

import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@Slf4j
@RequestMapping("api/v1/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("profile")
    public ResponseEntity<GetAdminProfileResponse> getAdminProfile(GetAdminProfileRequest request) {
        return adminService.getAdminProfile(request);
    }

    @PutMapping("profile")
    public ResponseEntity<UpdateAdminProfileResponse> updateAdminProfile(@RequestBody UpdateAdminProfileRequest request) {
        return adminService.updateAdminProfile(request);
    }

    @GetMapping("categories")
    public ResponseEntity<GetCategoriesResponse> getCategories(GetCategoriesRequest request) {
        return adminService.getCategories(request);
    }

    @PostMapping("category")
    public ResponseEntity<CreateCategoryResponse> createCategory(CreateCategoryRequest request) throws IOException {
        return adminService.createCategory(request);
    }

    @PutMapping("category")
    public ResponseEntity<UpdateCategoryResponse> updateCategory(UpdateCategoryRequest request) throws IOException {
        return adminService.updateCategory(request);
    }

    @GetMapping("services")
    public ResponseEntity<GetServicesResponse> getServices(GetServicesRequest request) {
        return adminService.getServices(request);
    }

    @PostMapping("service")
    public ResponseEntity<CreateServiceResponse> createService(CreateServiceRequest request) throws IOException {
        return adminService.createService(request);
    }

    @PutMapping("service")
    public ResponseEntity<UpdateServiceResponse> updateService(UpdateServiceRequest request) throws IOException {
        return adminService.updateService(request);
    }

    @GetMapping("search/services")
    public ResponseEntity<AdminSearchServicesResponse> searchServices(AdminSearchServicesRequest request) {
        return adminService.searchServices(request);
    }

    @GetMapping("subServices")
    public ResponseEntity<GetSubServicesResponse> getSubServices(GetSubServicesRequest request) {
        return adminService.getSubServices(request);
    }

    @PostMapping("subService")
    public ResponseEntity<CreateSubServiceResponse> createSubService(@RequestBody CreateSubServiceRequest request) {
        return adminService.createSubService(request);
    }

    @PutMapping("subService")
    public ResponseEntity<UpdateSubServiceResponse> updateSubService(@RequestBody UpdateSubServiceRequest request) {
        return adminService.updateSubService(request);
    }

    @GetMapping("requests")
    public ResponseEntity<AdminRequestingResponse> getRequests(AdminRequestingRequest request) {
        return adminService.getRequests(request);
    }

    @GetMapping("customers")
    public ResponseEntity<GetCustomersResponse> getCustomers(GetCustomersRequest request) {
        return adminService.getCustomers(request);
    }

    @GetMapping("repairers")
    public ResponseEntity<GetRepairersResponse> getRepairers(GetRepairersRequest request) {
        return adminService.getRepairers(request);
    }

    @GetMapping("customer")
    public ResponseEntity<GetCustomerDetailResponse> getCustomerDetail(GetCustomerDetailRequest request) {
        return adminService.getCustomerDetail(request);
    }

    @GetMapping("blackList")
    public ResponseEntity<GetBanUsersResponse> getBanUsers(GetBanUsersRequest request) {
        return adminService.getBanUsers(request);
    }

    @PostMapping("blackList")
    public ResponseEntity<BanUserResponse> banUser(@RequestBody BanUserRequest request) {
        return adminService.banUser(request);
    }

    @PostMapping("feedback")
    public ResponseEntity<AdminCreateFeedBackResponse> createFeedback(AdminCreateFeedBackRequest request) throws IOException {
        return adminService.createFeedback(request);
    }

    @GetMapping("feedback")
    public ResponseEntity<FeedbackDetailResponse> getFeedbackDetail(FeedbackDetailRequest request) {
        return adminService.getFeedbackDetail(request);
    }

    @GetMapping("accessories")
    public ResponseEntity<AdminGetAccessoriesResponse> getAccessories(AdminGetAccessoriesRequest request) {
        return adminService.getAccessories(request);
    }

    @GetMapping("CVList")
    public ResponseEntity<PendingRepairersResponse> getPendingRepairers(PendingRepairersRequest request) {
        return adminService.getPendingRepairers(request);
    }

    @PostMapping("accessory")
    public ResponseEntity<CreateAccessoryResponse> createAccessory(@RequestBody CreateAccessoryRequest request) {
        return adminService.createAccessory(request);
    }

    @PutMapping("accessory")
    public ResponseEntity<UpdateAccessoryResponse> updateAccessory(@RequestBody UpdateAccessoryRequest request) {
        return adminService.updateAccessory(request);
    }

    @PutMapping("feedback")
    public ResponseEntity<ResponseFeedbackResponse> responseFeedback(@RequestBody ResponseFeedbackRequest request) throws IOException {
        return adminService.responseFeedback(request);
    }

    @GetMapping("feedbacks")
    public ResponseEntity<FeedbacksResponse> getFeedbacks(FeedbacksRequest request) {
        return adminService.getFeedbacks(request);
    }

    @PutMapping("cv/accept")
    public ResponseEntity<AcceptCVResponse> acceptCV(@RequestBody AcceptCVRequest request) throws IOException {
        return adminService.acceptCV(request);
    }

    @GetMapping("repairer")
    public ResponseEntity<GetRepairerDetailResponse> getRepairerDetail(GetRepairerDetailRequest request) {
        return adminService.getRepairerDetail(request);
    }

    @GetMapping("search/categories")
    public ResponseEntity<SearchCategoriesResponse> searchCategories(SearchCategoriesRequest request) {
        return adminService.searchCategories(request);
    }

    @GetMapping("search/feedbacks")
    public ResponseEntity<SearchFeedbackResponse> searchFeedbacks(SearchFeedbackRequest request) {
        return adminService.searchFeedbacks(request);
    }

    @GetMapping("search/customers")
    public ResponseEntity<SearchCustomersResponse> searchCustomers(SearchCustomersRequest request) {
        return adminService.searchCustomers(request);
    }

    @GetMapping("search/repairers")
    public ResponseEntity<SearchRepairersResponse> searchRepairers(SearchRepairersRequest request) {
        return adminService.searchRepairers(request);
    }

    @GetMapping("search/accessories")
    public ResponseEntity<AdminSearchAccessoriesResponse> searchAccessories(AdminSearchAccessoriesRequest request) {
        return adminService.searchAccessories(request);
    }

    @GetMapping("transactions")
    public ResponseEntity<TransactionsResponse> getTransactions(TransactionsRequest request) {
        return adminService.getTransactions(request);
    }

    @GetMapping("request")
    public ResponseEntity<AdminGetRequestDetailResponse> getRequestDetail(AdminGetRequestDetailRequest request) {
        return adminService.getRequestDetail(request);
    }

    @GetMapping("search/subServices")
    public ResponseEntity<AdminSearchSubServicesResponse> searchSubServices(AdminSearchSubServicesRequest request) {
        return adminService.searchSubServices(request);
    }

    @GetMapping("transaction")
    public ResponseEntity<TransactionDetailResponse> getTransactionDetail(TransactionDetailRequest request) {
        return adminService.getTransactionDetail(request);
    }

    @GetMapping("search/transactions")
    public ResponseEntity<SearchTransactionsResponse> searchTransactions(SearchTransactionsRequest request) {
        return adminService.searchTransactions(request);
    }

    @PutMapping("withdraw/accept")
    public ResponseEntity<AcceptWithdrawResponse> acceptWithdraw(@RequestBody AcceptWithdrawRequest request) {
        return adminService.acceptWithdraw(request);
    }

    @GetMapping("repairer/withdraw/histories")
    public ResponseEntity<WithdrawHistoriesResponse> getRepairerWithdrawHistories(WithdrawHistoriesRequest request) {
        return adminService.getRepairerWithdrawHistories(request);
    }

    @PutMapping("withdraw/reject")
    public ResponseEntity<RejectWithdrawResponse> rejectWithdraw(@RequestBody RejectWithdrawRequest request) {
        return adminService.rejectWithdraw(request);
    }

    @PutMapping("cv/reject")
    public ResponseEntity<RejectCVResponse> rejectCV(@RequestBody RejectCVRequest request) {
        return adminService.rejectCV(request);
    }

    @DeleteMapping("blackList")
    public ResponseEntity<UnbanUserResponse> unbanUser(UnbanUserRequest request) {
        return adminService.unbanUser(request);
    }

    @GetMapping("withdraw/detail")
    public ResponseEntity<WithdrawDetailResponse> getRepairerWithdrawDetail(WithdrawDetailRequest request) {
        return adminService.getRepairerWithdrawDetail(request);
    }

    @GetMapping("repairer/withdraw/search")
    public ResponseEntity<SearchWithdrawResponse> searchRepairerWithdrawHistories(SearchWithdrawRequest request) {
        return adminService.searchRepairerWithdrawHistories(request);
    }

    @GetMapping("withdraw/pending/count")
    public ResponseEntity<CountPendingWithdrawsResponse> countPendingWithdraws() {
        return adminService.countPendingWithdraws();
    }

    @GetMapping("feedback/pending/count")
    public ResponseEntity<CountPendingFeedbacksResponse> countPendingFeedbacks() {
        return adminService.countPendingFeedbacks();
    }

    @GetMapping("search/request")
    public ResponseEntity<AdminSearchRequestResponse> searchRequests(AdminSearchRequestRequest request) {
        return adminService.searchRequests(request);
    }

    @GetMapping("category")
    public ResponseEntity<DetailCategoryResponse> getDetailCategory(DetailCategoryRequest request) {
        return adminService.getDetailCategory(request);
    }

    @GetMapping("service")
    public ResponseEntity<DetailServiceResponse> getDetailService(DetailServiceRequest request) {
        return adminService.getDetailService(request);
    }

    @GetMapping("subService")
    public ResponseEntity<DetailSubServiceResponse> getDetailSubService(DetailSubServiceRequest request) {
        return adminService.getDetailSubService(request);
    }

    @GetMapping("accessory")
    public ResponseEntity<DetailAccessoryResponse> getDetailAccessory(DetailAccessoryRequest request) {
        return adminService.getDetailAccessory(request);
    }
}
