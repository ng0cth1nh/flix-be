package com.fu.flix.service.impl;

import com.fu.flix.constant.enums.CommentType;
import com.fu.flix.constant.enums.RoleType;
import com.fu.flix.constant.enums.Status;
import com.fu.flix.dao.*;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.CommentRequest;
import com.fu.flix.dto.response.CommentResponse;
import com.fu.flix.entity.*;
import com.fu.flix.service.ConfirmedUserService;
import lombok.extern.slf4j.Slf4j;
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
    private final InvoiceDAO invoiceDAO;
    private final RepairRequestMatchingDAO repairRequestMatchingDAO;
    private final UserDAO userDAO;
    private final RepairRequestDAO repairRequestDAO;

    public ConfirmedUserServiceImpl(CommentDAO commentDAO,
                                    InvoiceDAO invoiceDAO,
                                    RepairRequestMatchingDAO repairRequestMatchingDAO,
                                    UserDAO userDAO,
                                    RepairRequestDAO repairRequestDAO) {
        this.commentDAO = commentDAO;
        this.invoiceDAO = invoiceDAO;
        this.repairRequestMatchingDAO = repairRequestMatchingDAO;
        this.userDAO = userDAO;
        this.repairRequestDAO = repairRequestDAO;
    }

    @Override
    public ResponseEntity<CommentResponse> createComment(CommentRequest request) {
        String requestCode = request.getRequestCode();

        if (requestCode == null) {
            throw new GeneralException(INVALID_REQUEST_CODE);
        }

        Optional<Invoice> optionalInvoice = invoiceDAO.findByRequestCode(requestCode);
        if (optionalInvoice.isEmpty()) {
            throw new GeneralException(INVALID_REQUEST_CODE);
        }

        Invoice invoice = optionalInvoice.get();
        if (!Status.DONE.getId().equals(invoice.getStatusId())) {
            throw new GeneralException(CAN_NOT_COMMENT_WHEN_STATUS_NOT_DONE);
        }

        User user = userDAO.findByUsername(request.getUsername()).get();
        String commentType = getCommentType(user.getRoles());
        Optional<Comment> optionalComment = commentDAO.findComment(requestCode, commentType);
        if (optionalComment.isPresent()) {
            throw new GeneralException(COMMENT_EXISTED);
        }

        RepairRequest repairRequest = repairRequestDAO.findByRequestCode(requestCode).get();
        RepairRequestMatching repairRequestMatching = repairRequestMatchingDAO.findByRequestCode(requestCode).get();

        Long userId = user.getId();
        Long customerId = repairRequest.getUserId();
        Long repairerId = repairRequestMatching.getRepairerId();
        if (!userId.equals(customerId) && !userId.equals(repairerId)) {
            throw new GeneralException(USER_AND_REQUEST_CODE_DOES_NOT_MATCH);
        }

        Comment comment = new Comment();
        comment.setRating(getRatingValidated(request.getRating()));
        comment.setComment(request.getComment());
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
            throw new GeneralException(RATING_IS_REQUIRED);
        }
        if (rating > 5 || rating < 1) {
            throw new GeneralException(RATING_MUST_IN_RANGE_1_TO_5);
        }
        return rating;
    }
}