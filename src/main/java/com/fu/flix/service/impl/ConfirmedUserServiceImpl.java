package com.fu.flix.service.impl;

import com.fu.flix.configuration.AppConf;
import com.fu.flix.constant.enums.CommentType;
import com.fu.flix.constant.enums.RoleType;
import com.fu.flix.constant.enums.RequestStatus;
import com.fu.flix.dao.*;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.CommentRequest;
import com.fu.flix.dto.response.CommentResponse;
import com.fu.flix.entity.*;
import com.fu.flix.service.ConfirmedUserService;
import com.fu.flix.service.ValidatorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Optional;

import static com.fu.flix.constant.Constant.*;
import static com.fu.flix.constant.Constant.RATING_MUST_IN_RANGE_1_TO_5;
import static com.fu.flix.constant.enums.RoleType.*;

@Service
@Slf4j
@Transactional
public class ConfirmedUserServiceImpl implements ConfirmedUserService {
    private final CommentDAO commentDAO;
    private final RepairRequestMatchingDAO repairRequestMatchingDAO;
    private final RepairRequestDAO repairRequestDAO;
    private final ValidatorService validatorService;
    private final AppConf appConf;

    public ConfirmedUserServiceImpl(CommentDAO commentDAO,
                                    RepairRequestMatchingDAO repairRequestMatchingDAO,
                                    RepairRequestDAO repairRequestDAO,
                                    ValidatorService validatorService,
                                    AppConf appConf) {
        this.commentDAO = commentDAO;
        this.repairRequestMatchingDAO = repairRequestMatchingDAO;
        this.repairRequestDAO = repairRequestDAO;
        this.validatorService = validatorService;
        this.appConf = appConf;
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
}
