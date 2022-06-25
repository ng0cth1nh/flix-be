package com.fu.flix.service.impl;

import com.fu.flix.configuration.AppConf;
import com.fu.flix.dao.*;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.RepairerApproveRequest;
import com.fu.flix.dto.response.RepairerApproveResponse;
import com.fu.flix.entity.Invoice;
import com.fu.flix.entity.RepairRequest;
import com.fu.flix.entity.RepairRequestMatching;
import com.fu.flix.entity.Repairer;
import com.fu.flix.service.RepairerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.fu.flix.constant.Constant.*;
import static com.fu.flix.constant.enums.Status.APPROVED;
import static com.fu.flix.constant.enums.Status.PENDING;

@Service
@Slf4j
@Transactional
public class RepairerServiceImpl implements RepairerService {
    private final RepairerDAO repairerDAO;
    private final RepairRequestDAO repairRequestDAO;
    private final RepairRequestMatchingDAO repairRequestMatchingDAO;
    private final InvoiceDAO invoiceDAO;
    private final AppConf appConf;
    private final ServiceDAO serviceDAO;

    public RepairerServiceImpl(RepairerDAO repairerDAO,
                               RepairRequestDAO repairRequestDAO,
                               RepairRequestMatchingDAO repairRequestMatchingDAO,
                               InvoiceDAO invoiceDAO,
                               AppConf appConf,
                               ServiceDAO serviceDAO) {
        this.repairerDAO = repairerDAO;
        this.repairRequestDAO = repairRequestDAO;
        this.repairRequestMatchingDAO = repairRequestMatchingDAO;
        this.invoiceDAO = invoiceDAO;
        this.appConf = appConf;
        this.serviceDAO = serviceDAO;
    }

    @Override
    public ResponseEntity<RepairerApproveResponse> approveRequest(RepairerApproveRequest request) {
        String requestCode = getRequestCode(request.getRequestCode());

        Optional<RepairRequest> optionalRepairRequest = repairRequestDAO.findByRequestCode(requestCode);

        if (optionalRepairRequest.isEmpty()) {
            throw new GeneralException(INVALID_REQUEST_CODE);
        }

        RepairRequest repairRequest = optionalRepairRequest.get();
        if (!PENDING.getId().equals(repairRequest.getStatusId())) {
            throw new GeneralException(JUST_CAN_ACCEPT_PENDING_REQUEST);
        }

        Repairer repairer = repairerDAO.findByUsername(request.getUsername()).get();
        if (repairer.isRepairing()) {
            throw new GeneralException(CAN_NOT_ACCEPT_REQUEST_WHEN_ON_ANOTHER_FIXING);
        }
        repairer.setRepairing(true);

        repairRequest.setStatusId(APPROVED.getId());

        RepairRequestMatching repairRequestMatching = buildRepairRequestMatching(requestCode, repairer.getUserId());
        repairRequestMatchingDAO.save(repairRequestMatching);

        RepairerApproveResponse response = new RepairerApproveResponse();
        response.setMessage(APPROVAL_REQUEST_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String getRequestCode(String requestCode) {
        return requestCode == null ? Strings.EMPTY : requestCode;
    }

    private RepairRequestMatching buildRepairRequestMatching(String requestCode, Long repairerId) {
        RepairRequestMatching repairRequestMatching = new RepairRequestMatching();
        repairRequestMatching.setRequestCode(requestCode);
        repairRequestMatching.setRepairerId(repairerId);
        return repairRequestMatching;
    }
}
