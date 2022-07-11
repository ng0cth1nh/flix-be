package com.fu.flix.service.impl;

import com.fu.flix.dao.RepairRequestDAO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.entity.RepairRequest;
import com.fu.flix.service.RequestService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.fu.flix.constant.Constant.INVALID_REQUEST_CODE;

@Service
public class RequestServiceImpl implements RequestService {
    private final RepairRequestDAO repairRequestDAO;

    public RequestServiceImpl(RepairRequestDAO repairRequestDAO) {
        this.repairRequestDAO = repairRequestDAO;
    }

    @Override
    public RepairRequest getRepairRequest(String requestCode) {
        if (isEmptyRequestCode(requestCode)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_REQUEST_CODE);
        }

        Optional<RepairRequest> optionalRepairRequest = repairRequestDAO.findByRequestCode(requestCode);
        if (optionalRepairRequest.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_REQUEST_CODE);
        }
        return optionalRepairRequest.get();
    }

    @Override
    public boolean isEmptyRequestCode(String requestCode) {
        return requestCode == null || requestCode.isEmpty();
    }
}
