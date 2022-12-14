package com.fu.flix.service.impl;

import com.fu.flix.configuration.AppConf;
import com.fu.flix.constant.Constant;
import com.fu.flix.dao.*;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.CreateFeedbackRequest;
import com.fu.flix.entity.*;
import com.fu.flix.service.ValidatorService;
import com.fu.flix.util.InputValidation;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.fu.flix.constant.Constant.*;
import static com.fu.flix.constant.enums.TransactionStatus.PENDING;
import static com.fu.flix.constant.enums.TransactionType.WITHDRAW;

@Service
public class ValidatorServiceImpl implements ValidatorService {
    private final UserDAO userDAO;
    private final AppConf appConf;
    private final ServiceDAO serviceDAO;
    private final CategoryDAO categoryDAO;
    private final SubServiceDAO subServiceDAO;
    private final RepairRequestDAO repairRequestDAO;
    private final AccessoryDAO accessoryDAO;
    private final FeedbackDAO feedbackDAO;
    private final TransactionHistoryDAO transactionHistoryDAO;
    private final Long NAME_MAX_LENGTH;
    private final Long DESCRIPTION_MAX_LENGTH;

    public ValidatorServiceImpl(UserDAO userDAO,
                                AppConf appConf,
                                ServiceDAO serviceDAO,
                                CategoryDAO categoryDAO,
                                SubServiceDAO subServiceDAO,
                                RepairRequestDAO repairRequestDAO,
                                AccessoryDAO accessoryDAO,
                                FeedbackDAO feedbackDAO,
                                TransactionHistoryDAO transactionHistoryDAO) {
        this.userDAO = userDAO;
        this.appConf = appConf;
        this.serviceDAO = serviceDAO;
        this.categoryDAO = categoryDAO;
        this.subServiceDAO = subServiceDAO;
        this.repairRequestDAO = repairRequestDAO;
        this.NAME_MAX_LENGTH = appConf.getNameMaxLength();
        this.DESCRIPTION_MAX_LENGTH = appConf.getDescriptionMaxLength();
        this.accessoryDAO = accessoryDAO;
        this.feedbackDAO = feedbackDAO;
        this.transactionHistoryDAO = transactionHistoryDAO;
    }

    @Override
    public User getUserValidated(String username) {
        if (username == null || username.isEmpty()) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, Constant.USER_NAME_IS_REQUIRED);
        }

        Optional<User> optionalUser = userDAO.findByUsername(username);
        return getUser(optionalUser);
    }

    @Override
    public User getUserValidated(Long userId) {
        if (userId == null) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, Constant.USER_ID_IS_REQUIRED);
        }

        Optional<User> optionalUser = userDAO.findById(userId);
        return getUser(optionalUser);
    }

    private User getUser(Optional<User> optionalUser) {
        if (optionalUser.isEmpty()) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, Constant.ACCOUNT_NOT_FOUND);
        }

        User user = optionalUser.get();
        if (!user.getIsActive()) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, Constant.USER_IS_INACTIVE);
        }
        return user;
    }

    @Override
    public Integer getPageSize(Integer pageSize) {
        pageSize = pageSize != null
                ? pageSize
                : this.appConf.getDefaultPageSize();
        if (pageSize < 1) {
            throw new GeneralException(HttpStatus.GONE, PAGE_SIZE_MUST_BE_GREATER_OR_EQUAL_1);
        }
        return pageSize;
    }

    @Override
    public Integer getPageNumber(Integer pageNumber) {
        pageNumber = pageNumber != null
                ? pageNumber
                : this.appConf.getDefaultPageNumber();
        if (pageNumber < 0) {
            throw new GeneralException(HttpStatus.GONE, PAGE_NUMBER_MUST_BE_GREATER_OR_EQUAL_0);
        }
        return pageNumber;
    }

    @Override
    public com.fu.flix.entity.Service getServiceValidated(Long serviceId) {
        if (serviceId == null) {
            throw new GeneralException(HttpStatus.GONE, INVALID_SERVICE);
        }

        Optional<com.fu.flix.entity.Service> optionalService = serviceDAO.findById(serviceId);
        if (optionalService.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_SERVICE);
        }
        return optionalService.get();
    }

    @Override
    public Category getCategoryValidated(Long categoryId) {
        if (categoryId == null) {
            throw new GeneralException(HttpStatus.GONE, INVALID_CATEGORY);
        }

        Optional<Category> optionalCategory = categoryDAO.findById(categoryId);
        if (optionalCategory.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_CATEGORY);
        }
        return optionalCategory.get();
    }

    @Override
    public SubService getSubServiceValidated(Long subServiceId) {
        if (subServiceId == null) {
            throw new GeneralException(HttpStatus.GONE, INVALID_SUB_SERVICE);
        }

        Optional<SubService> optionalSubService = subServiceDAO.findById(subServiceId);
        if (optionalSubService.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_SUB_SERVICE);
        }
        return optionalSubService.get();
    }

    @Override
    public void validateCreateFeedbackInput(CreateFeedbackRequest request) {
        InputValidation.getFeedbackTypeValidated(request.getFeedbackType());

        String requestCode = request.getRequestCode();
        if (requestCode != null && repairRequestDAO.findByRequestCode(requestCode).isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_REQUEST_CODE);
        }

        String title = request.getTitle();
        if (Strings.isEmpty(title) || title.length() > NAME_MAX_LENGTH) {
            throw new GeneralException(HttpStatus.GONE, INVALID_TITLE);
        }

        String description = request.getDescription();
        if (Strings.isEmpty(description) || description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new GeneralException(HttpStatus.GONE, INVALID_DESCRIPTION);
        }
    }

    @Override
    public Accessory getAccessoryValidated(Long accessoryId) {
        if (accessoryId == null) {
            throw new GeneralException(HttpStatus.GONE, INVALID_ACCESSORY);
        }

        Optional<Accessory> optionalAccessory = accessoryDAO.findById(accessoryId);
        if (optionalAccessory.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_ACCESSORY);
        }

        return optionalAccessory.get();
    }

    @Override
    public Feedback getFeedbackValidated(Long feedbackId) {
        if (feedbackId == null) {
            throw new GeneralException(HttpStatus.GONE, FEEDBACK_ID_IS_REQUIRED);
        }

        Optional<Feedback> optionalFeedback = feedbackDAO.findById(feedbackId);
        if (optionalFeedback.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_FEEDBACK_ID);
        }

        return optionalFeedback.get();
    }

    @Override
    public TransactionHistory getPendingWithdrawTransactionValidated(Long transactionId) {
        if (transactionId == null) {
            throw new GeneralException(HttpStatus.GONE, INVALID_TRANSACTION_ID);
        }

        Optional<TransactionHistory> optionalTransactionHistory = transactionHistoryDAO
                .findByIdAndTypeAndStatus(transactionId, WITHDRAW.name(), PENDING.name());
        if (optionalTransactionHistory.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, TRANSACTION_NOT_FOUND);
        }

        return optionalTransactionHistory.get();
    }
}
