package com.fu.flix.service.impl;

import com.fu.flix.constant.enums.StatisticalDateType;
import com.fu.flix.dao.InvoiceDAO;
import com.fu.flix.dao.RepairRequestHistoryDAO;
import com.fu.flix.dao.UserDAO;
import com.fu.flix.dto.*;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.StatisticalCustomerAccountsRequest;
import com.fu.flix.dto.request.StatisticalRepairerAccountsRequest;
import com.fu.flix.dto.request.StatisticalRequestsRequest;
import com.fu.flix.dto.request.StatisticalTransactionsRequest;
import com.fu.flix.dto.response.StatisticalCustomerAccountsResponse;
import com.fu.flix.dto.response.StatisticalRepairerAccountsResponse;
import com.fu.flix.dto.response.StatisticalRequestsResponse;
import com.fu.flix.dto.response.StatisticalTransactionsResponse;
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

@Service
@Transactional
public class StatisticalServiceImpl implements StatisticalService {
    private final UserDAO userDAO;
    private final InvoiceDAO invoiceDAO;
    private final RepairRequestHistoryDAO repairRequestHistoryDAO;
    private final String DAY_FORMAT = "dd/MM/yyyy";
    private final String MONTH_FORMAT = "MM/yyyy";
    private final String YEAR_FORMAT = "yyyy";

    public StatisticalServiceImpl(UserDAO userDAO,
                                  InvoiceDAO invoiceDAO,
                                  RepairRequestHistoryDAO repairRequestHistoryDAO) {
        this.userDAO = userDAO;
        this.invoiceDAO = invoiceDAO;
        this.repairRequestHistoryDAO = repairRequestHistoryDAO;
    }

    @Override
    public ResponseEntity<StatisticalCustomerAccountsResponse> getStatisticalCustomerAccounts(StatisticalCustomerAccountsRequest request) {
        StatisticalDateType type = getStatisticalDateTypeValidated(request.getType());
        LocalDateTime fromValidated = InputValidation.getFromValidated(request.getFrom(), type);
        LocalDateTime toValidated = InputValidation.getToValidated(request.getTo(), fromValidated, type);

        List<StatisticalCustomerAccountDTO> data = queryStatisticalCustomerAccounts(fromValidated, toValidated, type);

        StatisticalCustomerAccountsResponse response = new StatisticalCustomerAccountsResponse();
        response.setData(data);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private List<StatisticalCustomerAccountDTO> queryStatisticalCustomerAccounts(LocalDateTime fromValidated,
                                                                                 LocalDateTime toValidated,
                                                                                 StatisticalDateType type) {
        List<StatisticalCustomerAccountDTO> data = new ArrayList<>();

        while (fromValidated.isBefore(toValidated)) {
            StatisticalCustomerAccountDTO dto = new StatisticalCustomerAccountDTO();
            LocalDateTime fromNext = getFromNext(fromValidated, type);

            IStatisticalCustomerAccountDTO statisticalCustomerAccount = userDAO
                    .findStatisticalCustomerAccounts(fromValidated, fromNext);

            dto.setDate(getQueryDayFormatted(fromValidated, type));
            dto.setTotalBanAccount(statisticalCustomerAccount.getTotalBanAccount());
            dto.setTotalNewAccount(statisticalCustomerAccount.getTotalNewAccount());

            data.add(dto);
            fromValidated = fromNext;
        }

        return data;
    }

    @Override
    public ResponseEntity<StatisticalRepairerAccountsResponse> getStatisticalRepairerAccounts(StatisticalRepairerAccountsRequest request) {
        StatisticalDateType type = getStatisticalDateTypeValidated(request.getType());
        LocalDateTime fromValidated = InputValidation.getFromValidated(request.getFrom(), type);
        LocalDateTime toValidated = InputValidation.getToValidated(request.getTo(), fromValidated, type);

        List<StatisticalRepairerAccountDTO> data = queryStatisticalRepairerAccounts(fromValidated, toValidated, type);

        StatisticalRepairerAccountsResponse response = new StatisticalRepairerAccountsResponse();
        response.setData(data);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private List<StatisticalRepairerAccountDTO> queryStatisticalRepairerAccounts(LocalDateTime fromValidated,
                                                                                 LocalDateTime toValidated,
                                                                                 StatisticalDateType type) {
        List<StatisticalRepairerAccountDTO> data = new ArrayList<>();

        while (fromValidated.isBefore(toValidated)) {
            StatisticalRepairerAccountDTO dto = new StatisticalRepairerAccountDTO();
            LocalDateTime fromNext = getFromNext(fromValidated, type);

            IStatisticalRepairerAccountDTO statisticalRepairerAccount = userDAO
                    .findStatisticalRepairerAccounts(fromValidated, fromNext);

            dto.setDate(getQueryDayFormatted(fromValidated, type));
            dto.setTotalNewAccount(statisticalRepairerAccount.getTotalNewAccount());
            dto.setTotalBanAccount(statisticalRepairerAccount.getTotalBanAccount());
            dto.setTotalRejectedAccount(statisticalRepairerAccount.getTotalRejectedAccount());
            dto.setTotalApprovedAccount(statisticalRepairerAccount.getTotalApprovedAccount());

            data.add(dto);
            fromValidated = fromNext;
        }

        return data;
    }

    @Override
    public ResponseEntity<StatisticalRequestsResponse> getStatisticalRequests(StatisticalRequestsRequest request) {
        StatisticalDateType type = getStatisticalDateTypeValidated(request.getType());
        LocalDateTime fromValidated = InputValidation.getFromValidated(request.getFrom(), type);
        LocalDateTime toValidated = InputValidation.getToValidated(request.getTo(), fromValidated, type);

        List<StatisticalRequestDTO> data = queryStatisticalRequests(fromValidated, toValidated, type);

        StatisticalRequestsResponse response = new StatisticalRequestsResponse();
        response.setData(data);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private List<StatisticalRequestDTO> queryStatisticalRequests(LocalDateTime fromValidated,
                                                                 LocalDateTime toValidated,
                                                                 StatisticalDateType type) {
        List<StatisticalRequestDTO> data = new ArrayList<>();

        while (fromValidated.isBefore(toValidated)) {
            StatisticalRequestDTO dto = new StatisticalRequestDTO();
            LocalDateTime fromNext = getFromNext(fromValidated, type);

            dto.setDate(getQueryDayFormatted(fromValidated, type));
            dto.setTotalPendingRequest(repairRequestHistoryDAO.countTotalRequestHistoriesByType(fromValidated,
                    fromNext,
                    PENDING_REQUEST.name()));
            dto.setTotalApprovedRequest(repairRequestHistoryDAO.countTotalRequestHistoriesByType(fromValidated,
                    fromNext,
                    APPROVED_REQUEST.name()));
            dto.setTotalFixingRequest(repairRequestHistoryDAO.countTotalRequestHistoriesByType(fromValidated,
                    fromNext,
                    FIXING_REQUEST.name()));
            dto.setTotalDoneRequest(repairRequestHistoryDAO.countTotalRequestHistoriesByType(fromValidated,
                    fromNext,
                    DONE_REQUEST.name()));
            dto.setTotalPaymentWaitingRequest(repairRequestHistoryDAO.countTotalRequestHistoriesByType(fromValidated,
                    fromNext,
                    PAYMENT_WAITING_REQUEST.name()));
            dto.setTotalCancelRequest(repairRequestHistoryDAO.countTotalRequestHistoriesByType(fromValidated,
                    fromNext,
                    CANCELLED_REQUEST.name()));

            data.add(dto);
            fromValidated = fromNext;
        }

        return data;
    }

    @Override
    public ResponseEntity<StatisticalTransactionsResponse> getStatisticalTransactions(StatisticalTransactionsRequest request) {
        StatisticalDateType type = getStatisticalDateTypeValidated(request.getType());
        LocalDateTime fromValidated = InputValidation.getFromValidated(request.getFrom(), type);
        LocalDateTime toValidated = InputValidation.getToValidated(request.getTo(), fromValidated, type);

        List<StatisticalTransactionDTO> data = queryStatisticalTransactions(fromValidated, toValidated, type);

        StatisticalTransactionsResponse response = new StatisticalTransactionsResponse();
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

    private List<StatisticalTransactionDTO> queryStatisticalTransactions(LocalDateTime fromValidated,
                                                                         LocalDateTime toValidated,
                                                                         StatisticalDateType type) {
        List<StatisticalTransactionDTO> data = new ArrayList<>();

        while (fromValidated.isBefore(toValidated)) {
            StatisticalTransactionDTO dto = new StatisticalTransactionDTO();
            LocalDateTime fromNext = getFromNext(fromValidated, type);

            IStatisticalTransactionDTO statisticalTransaction = invoiceDAO.findStatisticalTransactionDTO(fromValidated, fromNext);

            dto.setDate(getQueryDayFormatted(fromValidated, type));
            dto.setTotalRevenue(statisticalTransaction.getTotalRevenue());
            dto.setTotalProfit(statisticalTransaction.getTotalProfit());

            data.add(dto);
            fromValidated = fromNext;
        }

        return data;
    }

    private String getQueryDayFormatted(LocalDateTime fromValidated, StatisticalDateType type) {
        switch (type) {
            case DAY:
                return DateFormatUtil.toString(fromValidated, DAY_FORMAT);
            case MONTH:
                return DateFormatUtil.toString(fromValidated, MONTH_FORMAT);
            default:
                return DateFormatUtil.toString(fromValidated, YEAR_FORMAT);
        }
    }

    private LocalDateTime getFromNext(LocalDateTime fromValidated, StatisticalDateType type) {
        switch (type) {
            case DAY:
                return fromValidated.plusDays(1);
            case MONTH:
                return fromValidated.plusMonths(1);
            default:
                return fromValidated.plusYears(1);
        }
    }
}
