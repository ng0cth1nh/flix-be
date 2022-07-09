package com.fu.flix.service.impl;

import com.fu.flix.configuration.AppConf;
import com.fu.flix.constant.enums.CommentType;
import com.fu.flix.constant.enums.RoleType;
import com.fu.flix.constant.enums.RequestStatus;
import com.fu.flix.dao.*;
import com.fu.flix.dto.*;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.CommentRequest;
import com.fu.flix.dto.request.GetFixedServiceRequest;
import com.fu.flix.dto.request.GetInvoiceRequest;
import com.fu.flix.dto.response.CommentResponse;
import com.fu.flix.dto.response.GetFixedServiceResponse;
import com.fu.flix.dto.response.GetInvoiceResponse;
import com.fu.flix.entity.*;
import com.fu.flix.service.*;
import com.fu.flix.util.DateFormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fu.flix.constant.Constant.*;
import static com.fu.flix.constant.Constant.RATING_MUST_IN_RANGE_1_TO_5;
import static com.fu.flix.constant.enums.RequestStatus.DONE;
import static com.fu.flix.constant.enums.RequestStatus.PAYMENT_WAITING;
import static com.fu.flix.constant.enums.RoleType.*;

@Service
@Slf4j
@Transactional
public class ConfirmedUserServiceImpl implements ConfirmedUserService {
    private final CommentDAO commentDAO;
    private final RepairRequestMatchingDAO repairRequestMatchingDAO;
    private final RepairRequestDAO repairRequestDAO;
    private final ValidatorService validatorService;
    private final InvoiceDAO invoiceDAO;
    private final AppConf appConf;
    private final AddressService addressService;
    private final VoucherService voucherService;
    private final RequestService requestService;
    private final ExtraServiceDAO extraServiceDAO;
    private final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public ConfirmedUserServiceImpl(CommentDAO commentDAO,
                                    RepairRequestMatchingDAO repairRequestMatchingDAO,
                                    RepairRequestDAO repairRequestDAO,
                                    ValidatorService validatorService,
                                    InvoiceDAO invoiceDAO,
                                    AppConf appConf,
                                    AddressService addressService,
                                    VoucherService voucherService,
                                    RequestService requestService,
                                    ExtraServiceDAO extraServiceDAO) {
        this.commentDAO = commentDAO;
        this.repairRequestMatchingDAO = repairRequestMatchingDAO;
        this.repairRequestDAO = repairRequestDAO;
        this.validatorService = validatorService;
        this.invoiceDAO = invoiceDAO;
        this.appConf = appConf;
        this.addressService = addressService;
        this.voucherService = voucherService;
        this.requestService = requestService;
        this.extraServiceDAO = extraServiceDAO;
    }

    @Override
    public ResponseEntity<CommentResponse> createComment(CommentRequest request) {
        String requestCode = request.getRequestCode();
        if (requestCode == null || requestCode.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_REQUEST_CODE);
        }

        String commentContent = request.getComment() == null
                ? Strings.EMPTY
                : request.getComment();
        if (commentContent.length() > this.appConf.getDescriptionMaxLength()) {
            throw new GeneralException(HttpStatus.GONE, EXCEEDED_COMMENT_LENGTH_ALLOWED);
        }

        Optional<RepairRequest> optionalRepairRequest = repairRequestDAO.findByRequestCode(requestCode);
        if (optionalRepairRequest.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_REQUEST_CODE);
        }

        RepairRequest repairRequest = optionalRepairRequest.get();
        if (!RequestStatus.DONE.getId().equals(repairRequest.getStatusId())) {
            throw new GeneralException(HttpStatus.CONFLICT, CAN_NOT_COMMENT_WHEN_STATUS_NOT_DONE);
        }

        User user = validatorService.getUserValidated(request.getUsername());
        String commentType = getCommentType(user.getRoles());
        Optional<Comment> optionalComment = commentDAO.findComment(requestCode, commentType);
        if (optionalComment.isPresent()) {
            throw new GeneralException(HttpStatus.CONFLICT, COMMENT_EXISTED);
        }

        RepairRequestMatching repairRequestMatching = repairRequestMatchingDAO.findByRequestCode(requestCode).get();

        Long userId = user.getId();
        Long customerId = repairRequest.getUserId();
        Long repairerId = repairRequestMatching.getRepairerId();
        if (!userId.equals(customerId) && !userId.equals(repairerId)) {
            throw new GeneralException(HttpStatus.GONE, USER_AND_REQUEST_CODE_DOES_NOT_MATCH);
        }

        Comment comment = new Comment();
        comment.setRating(getRatingValidated(request.getRating()));
        comment.setComment(commentContent);
        comment.setRequestCode(requestCode);
        comment.setType(commentType);

        commentDAO.save(comment);

        CommentResponse response = new CommentResponse();
        response.setMessage(COMMENT_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String getCommentType(Collection<Role> roles) {
        for (Role role : roles) {
            RoleType roleType = valueOf(role.getName());
            if (ROLE_CUSTOMER.equals(roleType)) {
                return CommentType.CUSTOMER_COMMENT.name();
            } else if (ROLE_REPAIRER.equals(roleType)) {
                return CommentType.REPAIRER_COMMENT.name();
            }
        }
        return null;
    }

    private Integer getRatingValidated(Integer rating) {
        if (rating == null) {
            throw new GeneralException(HttpStatus.GONE, RATING_IS_REQUIRED);
        }
        if (rating > 5 || rating < 1) {
            throw new GeneralException(HttpStatus.GONE, RATING_MUST_IN_RANGE_1_TO_5);
        }
        return rating;
    }

    @Override
    public ResponseEntity<GetInvoiceResponse> getInvoice(GetInvoiceRequest request) {
        String requestCode = request.getRequestCode();
        if (requestCode == null || requestCode.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_REQUEST_CODE);
        }

        Long userId = request.getUserId();
        GetInvoiceResponse response = new GetInvoiceResponse();
        Optional<IInvoiceDTO> optionalInvoiceDTO;
        if (isCustomer(request.getRoles())) {
            optionalInvoiceDTO = invoiceDAO.findCustomerInvoice(requestCode, userId);
        } else {
            optionalInvoiceDTO = invoiceDAO.findRepairerInvoice(requestCode, userId);
        }

        if (optionalInvoiceDTO.isPresent()) {
            IInvoiceDTO dto = optionalInvoiceDTO.get();
            VoucherDTO voucherInfo = voucherService.getVoucherInfo(dto.getVoucherId());

            response.setCustomerName(dto.getCustomerName());
            response.setCustomerAvatar(dto.getCustomerAvatar());
            response.setCustomerPhone(dto.getCustomerPhone());
            response.setCustomerAddress(addressService.getAddressFormatted(dto.getCustomerAddressId()));
            response.setRepairerName(dto.getRepairerName());
            response.setRepairerAvatar(dto.getRepairerAvatar());
            response.setRepairerPhone(dto.getRepairerPhone());
            response.setRepairerAddress(addressService.getAddressFormatted(dto.getRepairerAddressId()));
            response.setTotalExtraServicePrice(dto.getTotalExtraServicePrice());
            response.setTotalAccessoryPrice(dto.getTotalAccessoryPrice());
            response.setTotalSubServicePrice(dto.getTotalSubServicePrice());
            response.setInspectionPrice(dto.getInspectionPrice());
            response.setTotalDiscount(dto.getTotalDiscount());
            response.setExpectFixingTime(DateFormatUtil.toString(dto.getExpectFixingTime(), DATE_TIME_PATTERN));
            response.setVoucherDescription(voucherInfo.getVoucherDescription());
            response.setVoucherDiscount(voucherInfo.getVoucherDiscount());
            response.setPaymentMethod(dto.getPaymentMethod());
            response.setRequestCode(requestCode);
            response.setCreatedAt(DateFormatUtil.toString(dto.getCreatedAt(), DATE_TIME_PATTERN));
            response.setActualPrice(dto.getActualPrice());
            response.setTotalPrice(dto.getTotalPrice());
            response.setVatPrice(dto.getVatPrice());
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<GetFixedServiceResponse> getFixedServices(GetFixedServiceRequest request) {
        String requestCode = request.getRequestCode();
        RepairRequest repairRequest = requestService.getRepairRequest(requestCode);
        Long userId = request.getUserId();

        if (canNotGetFixedServices(repairRequest.getStatusId())) {
            throw new GeneralException(HttpStatus.GONE, JUST_CAN_GET_FIXED_SERVICES_WHEN_REQUEST_STATUS_IS_PAYMENT_WAITING_OR_DONE);
        }

        if (isCustomer(request.getRoles())) {
            if (!userId.equals(repairRequest.getUserId())) {
                throw new GeneralException(HttpStatus.GONE, CUSTOMER_DOES_NOT_HAVE_PERMISSION_TO_GET_FIXED_SERVICES_FOR_THIS_INVOICE);
            }
        } else {
            RepairRequestMatching repairRequestMatching = repairRequestMatchingDAO.findByRequestCode(requestCode).get();
            if (!userId.equals(repairRequestMatching.getRepairerId())) {
                throw new GeneralException(HttpStatus.GONE, REPAIRER_DOES_NOT_HAVE_PERMISSION_TO_GET_FIXED_SERVICES_FOR_THIS_INVOICE);
            }
        }

        Invoice invoice = invoiceDAO.findByRequestCode(requestCode).get();
        GetFixedServiceResponse response = new GetFixedServiceResponse();
        List<SubServiceOutputDTO> subServiceDTOs = getSubServiceDTOs(invoice);
        List<AccessoryOutputDTO> accessoryDTOs = getAccessoryDTOs(invoice);
        List<ExtraServiceOutputDTO> extraServiceDTOs = getExtraServiceDTOs(requestCode);
        response.setExtraServices(extraServiceDTOs);
        response.setSubServices(subServiceDTOs);
        response.setAccessories(accessoryDTOs);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private boolean canNotGetFixedServices(String statusId) {
        return !PAYMENT_WAITING.getId().equals(statusId) && !DONE.getId().equals(statusId);
    }

    private boolean isCustomer(String[] roles) {
        return Arrays.stream(roles).anyMatch(r -> ROLE_CUSTOMER.name().equals(r));
    }

    private List<SubServiceOutputDTO> getSubServiceDTOs(Invoice invoice) {
        return invoice.getSubServices().stream()
                .map(subService -> {
                    SubServiceOutputDTO dto = new SubServiceOutputDTO();
                    dto.setId(subService.getId());
                    dto.setName(subService.getName());
                    dto.setPrice(subService.getPrice());
                    return dto;
                }).collect(Collectors.toList());
    }

    private List<AccessoryOutputDTO> getAccessoryDTOs(Invoice invoice) {
        return invoice.getAccessories().stream()
                .map(accessory -> {
                    AccessoryOutputDTO dto = new AccessoryOutputDTO();
                    dto.setId(accessory.getId());
                    dto.setName(accessory.getName());
                    dto.setPrice(accessory.getPrice());
                    dto.setInsuranceTime(accessory.getInsuranceTime());
                    return dto;
                }).collect(Collectors.toList());
    }

    private List<ExtraServiceOutputDTO> getExtraServiceDTOs(String requestCode) {
        return extraServiceDAO.findByRequestCode(requestCode).stream()
                .map(extraService -> {
                    ExtraServiceOutputDTO dto = new ExtraServiceOutputDTO();
                    dto.setId(extraService.getId());
                    dto.setName(extraService.getName());
                    dto.setPrice(extraService.getPrice());
                    dto.setDescription(extraService.getDescription());
                    dto.setInsuranceTime(extraService.getInsuranceTime());
                    return dto;
                }).collect(Collectors.toList());
    }
}
