package com.fu.flix.service.impl;

import com.fu.flix.constant.enums.RepairRequestHistoryType;
import com.fu.flix.constant.enums.StatisticalDateType;
import com.fu.flix.dao.RepairRequestHistoryDAO;
import com.fu.flix.dao.UserDAO;
import com.fu.flix.dao.UserUpdateHistoryDAO;
import com.fu.flix.dto.StatisticalCustomerAccountDTO;
import com.fu.flix.dto.StatisticalRepairerAccountDTO;
import com.fu.flix.dto.StatisticalRequestDTO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.StatisticalCustomerAccountsRequest;
import com.fu.flix.dto.request.StatisticalRepairerAccountsRequest;
import com.fu.flix.dto.request.StatisticalRequestsRequest;
import com.fu.flix.dto.response.StatisticalCustomerAccountsResponse;
import com.fu.flix.dto.response.StatisticalRepairerAccountsResponse;
import com.fu.flix.dto.response.StatisticalRequestsResponse;
import com.fu.flix.service.StatisticalService;
import com.fu.flix.util.DateFormatUtil;
import com.fu.flix.util.InputValidation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.fu.flix.constant.Constant.INVALID_DATE_TYPE;
import static com.fu.flix.constant.enums.RepairRequestHistoryType.*;
import static com.fu.flix.constant.enums.RoleType.*;

@Service
@Transactional
public class StatisticalServiceImpl implements StatisticalService {
    private final UserDAO userDAO;
    private final UserUpdateHistoryDAO userUpdateHistoryDAO;

    private final RepairRequestHistoryDAO repairRequestHistoryDAO;
    private final String DAY_FORMAT = "dd/MM/yyyy";
    private final String MONTH_FORMAT = "MM/yyyy";
    private final String YEAR_FORMAT = "yyyy";

    public StatisticalServiceImpl(UserDAO userDAO,
                                  UserUpdateHistoryDAO userUpdateHistoryDAO,
                                  RepairRequestHistoryDAO repairRequestHistoryDAO) {
        this.userDAO = userDAO;
        this.userUpdateHistoryDAO = userUpdateHistoryDAO;
        this.repairRequestHistoryDAO = repairRequestHistoryDAO;
    }

    @Override
    public ResponseEntity<StatisticalCustomerAccountsResponse> getStatisticalCustomerAccounts(StatisticalCustomerAccountsRequest request) {
        StatisticalDateType type = getStatisticalDateTypeValidated(request.getType());
        LocalDateTime fromDateValidated = InputValidation.getFromDateValidated(request.getFrom(), type);
        LocalDateTime toDateValidated = InputValidation.getToDateValidated(request.getTo(), fromDateValidated, type);

        List<StatisticalCustomerAccountDTO> data = queryStatisticalCustomerAccounts(fromDateValidated, toDateValidated, type);

        StatisticalCustomerAccountsResponse response = new StatisticalCustomerAccountsResponse();
        response.setData(data);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private StatisticalDateType getStatisticalDateTypeValidated(String type) {
        for (StatisticalDateType stt : StatisticalDateType.values()) {
            if (stt.name().equals(type)) {
                return stt;
            }
        }
        throw new GeneralException(HttpStatus.GONE, INVALID_DATE_TYPE);
    }

    private List<StatisticalCustomerAccountDTO> queryStatisticalCustomerAccounts(LocalDateTime fromDateValidated,
                                                                                 LocalDateTime toDateValidated,
                                                                                 StatisticalDateType type) {
        List<StatisticalCustomerAccountDTO> data = new ArrayList<>();

        while (fromDateValidated.isBefore(toDateValidated)) {
            StatisticalCustomerAccountDTO dto = new StatisticalCustomerAccountDTO();
            LocalDateTime flagDateTimeNext = getFromDateTimeNext(fromDateValidated, type);

            dto.setDate(getQueryDayFormatted(fromDateValidated, type));
            dto.setTotalNewAccount(userDAO.countTotalCreatedAccounts(fromDateValidated,
                    flagDateTimeNext,
                    ROLE_CUSTOMER.getId()));
            dto.setTotalBanAccount(userUpdateHistoryDAO.countTotalBannedAccountHistories(fromDateValidated,
                    flagDateTimeNext,
                    ROLE_CUSTOMER.getId()));
            data.add(dto);
            fromDateValidated = flagDateTimeNext;
        }

        return data;
    }

    @Override
    public ResponseEntity<StatisticalRepairerAccountsResponse> getStatisticalRepairerAccounts(StatisticalRepairerAccountsRequest request) {
        StatisticalDateType type = getStatisticalDateTypeValidated(request.getType());
        LocalDateTime fromDateValidated = InputValidation.getFromDateValidated(request.getFrom(), type);
        LocalDateTime toDateValidated = InputValidation.getToDateValidated(request.getTo(), fromDateValidated, type);

        List<StatisticalRepairerAccountDTO> data = queryStatisticalRepairerAccounts(fromDateValidated, toDateValidated, type);

        StatisticalRepairerAccountsResponse response = new StatisticalRepairerAccountsResponse();
        response.setData(data);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private List<StatisticalRepairerAccountDTO> queryStatisticalRepairerAccounts(LocalDateTime fromDateValidated,
                                                                                 LocalDateTime toDateValidated,
                                                                                 StatisticalDateType type) {
        List<StatisticalRepairerAccountDTO> data = new ArrayList<>();

        while (fromDateValidated.isBefore(toDateValidated)) {
            StatisticalRepairerAccountDTO dto = new StatisticalRepairerAccountDTO();
            LocalDateTime flagDateTimeNext = getFromDateTimeNext(fromDateValidated, type);

            dto.setDate(getQueryDayFormatted(fromDateValidated, type));
            dto.setTotalNewAccount(userDAO.countTotalCreatedAccounts(fromDateValidated,
                    flagDateTimeNext,
                    ROLE_REPAIRER.getId(),
                    ROLE_PENDING_REPAIRER.getId()));
            dto.setTotalBanAccount(userUpdateHistoryDAO.countTotalBannedAccountHistories(fromDateValidated,
                    flagDateTimeNext,
                    ROLE_REPAIRER.getId(),
                    ROLE_PENDING_REPAIRER.getId()));
            dto.setTotalApprovedAccount(userUpdateHistoryDAO.countTotalApprovedAccountHistories(fromDateValidated,
                    flagDateTimeNext));
            dto.setTotalRejectedAccount(userUpdateHistoryDAO.countTotalRejectedAccountHistories(fromDateValidated,
                    flagDateTimeNext));

            data.add(dto);
            fromDateValidated = flagDateTimeNext;
        }

        return data;
    }

    @Override
    public ResponseEntity<StatisticalRequestsResponse> getStatisticalRequests(StatisticalRequestsRequest request) {
        StatisticalDateType type = getStatisticalDateTypeValidated(request.getType());
        LocalDateTime fromDateValidated = InputValidation.getFromDateValidated(request.getFrom(), type);
        LocalDateTime toDateValidated = InputValidation.getToDateValidated(request.getTo(), fromDateValidated, type);

        List<StatisticalRequestDTO> data = queryStatisticalRequests(fromDateValidated, toDateValidated, type);

        StatisticalRequestsResponse response = new StatisticalRequestsResponse();
        response.setData(data);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private List<StatisticalRequestDTO> queryStatisticalRequests(LocalDateTime fromDateValidated,
                                                                 LocalDateTime toDateValidated,
                                                                 StatisticalDateType type) {
        List<StatisticalRequestDTO> data = new ArrayList<>();

        while (fromDateValidated.isBefore(toDateValidated)) {
            StatisticalRequestDTO dto = new StatisticalRequestDTO();
            LocalDateTime flagDateTimeNext = getFromDateTimeNext(fromDateValidated, type);

            dto.setDate(getQueryDayFormatted(fromDateValidated, type));
            dto.setTotalPendingRequest(repairRequestHistoryDAO.countTotalRequestHistoriesByType(fromDateValidated,
                    flagDateTimeNext,
                    PENDING_REQUEST.name()));
            dto.setTotalApprovedRequest(repairRequestHistoryDAO.countTotalRequestHistoriesByType(fromDateValidated,
                    flagDateTimeNext,
                    APPROVED_REQUEST.name()));
            dto.setTotalFixingRequest(repairRequestHistoryDAO.countTotalRequestHistoriesByType(fromDateValidated,
                    flagDateTimeNext,
                    FIXING_REQUEST.name()));
            dto.setTotalDoneRequest(repairRequestHistoryDAO.countTotalRequestHistoriesByType(fromDateValidated,
                    flagDateTimeNext,
                    DONE_REQUEST.name()));
            dto.setTotalPaymentWaitingRequest(repairRequestHistoryDAO.countTotalRequestHistoriesByType(fromDateValidated,
                    flagDateTimeNext,
                    PAYMENT_WAITING_REQUEST.name()));

            data.add(dto);
            fromDateValidated = flagDateTimeNext;
        }

        return data;
    }

    private String getQueryDayFormatted(LocalDateTime fromDateValidated, StatisticalDateType type) {
        switch (type) {
            case DAY:
                return DateFormatUtil.toString(fromDateValidated, DAY_FORMAT);
            case MONTH:
                return DateFormatUtil.toString(fromDateValidated, MONTH_FORMAT);
            default:
                return DateFormatUtil.toString(fromDateValidated, YEAR_FORMAT);
        }
    }

    private LocalDateTime getFromDateTimeNext(LocalDateTime fromDateValidated, StatisticalDateType type) {
        switch (type) {
            case DAY:
                return fromDateValidated.plusDays(1);
            case MONTH:
                return fromDateValidated.plusMonths(1);
            default:
                return fromDateValidated.plusYears(1);
        }
    }
}
