package com.fu.flix.service.impl;

import com.fu.flix.constant.enums.LocationType;
import com.fu.flix.constant.enums.RepairerSuggestionType;
import com.fu.flix.dao.*;
import com.fu.flix.dto.AccessoryOutputDTO;
import com.fu.flix.dto.IRequestingDTO;
import com.fu.flix.dto.RequestingDTO;
import com.fu.flix.dto.SubServiceOutputDTO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.RequestingFilterRequest;
import com.fu.flix.dto.request.RequestingSuggestionRequest;
import com.fu.flix.dto.request.SearchAccessoriesRequest;
import com.fu.flix.dto.request.SearchSubServicesRequest;
import com.fu.flix.dto.response.RequestingFilterResponse;
import com.fu.flix.dto.response.RequestingSuggestionResponse;
import com.fu.flix.dto.response.SearchAccessoriesResponse;
import com.fu.flix.dto.response.SearchSubServicesResponse;
import com.fu.flix.entity.*;
import com.fu.flix.service.AddressService;
import com.fu.flix.service.CommonRepairerService;
import com.fu.flix.util.DateFormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.fu.flix.constant.Constant.*;
import static com.fu.flix.constant.enums.RepairerSuggestionType.SUGGESTED;


@org.springframework.stereotype.Service
@Slf4j
@Transactional
public class CommonRepairerServiceImpl implements CommonRepairerService {
    private final UserAddressDAO userAddressDAO;
    private final RepairerDAO repairerDAO;
    private final RepairRequestDAO repairRequestDAO;
    private final AddressService addressService;
    private final SubServiceDAO subServiceDAO;
    private final AccessoryDAO accessoryDAO;
    private final String DATE_PATTERN = "dd-MM-yyyy";
    private final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public CommonRepairerServiceImpl(UserAddressDAO userAddressDAO,
                                     RepairerDAO repairerDAO,
                                     RepairRequestDAO repairRequestDAO,
                                     AddressService addressService,
                                     SubServiceDAO subServiceDAO,
                                     AccessoryDAO accessoryDAO) {
        this.userAddressDAO = userAddressDAO;
        this.repairerDAO = repairerDAO;
        this.repairRequestDAO = repairRequestDAO;
        this.addressService = addressService;
        this.subServiceDAO = subServiceDAO;
        this.accessoryDAO = accessoryDAO;
    }

    @Override
    public ResponseEntity<RequestingSuggestionResponse> getSuggestionRequestList(RequestingSuggestionRequest request) {
        String type = getRepairerSuggestionTypeValidated(request.getType());

        Long userId = request.getUserId();
        UserAddress userAddress = userAddressDAO.findByUserIdAndIsMainAddressAndDeletedAtIsNull(userId, true).get();
        Long userAddressId = userAddress.getId();

        Repairer repairer = repairerDAO.findByUserId(userId).get();
        Collection<Service> services = repairer.getServices();
        List<Long> serviceIds = services.stream()
                .map(com.fu.flix.entity.Service::getId)
                .collect(Collectors.toList());

        List<IRequestingDTO> iRequestingDTOS;
        if (SUGGESTED.name().equals(type)) {
            String districtId = userAddressDAO.findDistrictIdByUserAddressId(userAddressId);
            iRequestingDTOS = repairRequestDAO.findPendingRequestByDistrict(serviceIds, districtId);
        } else {
            String cityId = userAddressDAO.findCityIdByUserAddressId(userAddressId);
            iRequestingDTOS = repairRequestDAO.findPendingRequestByCity(serviceIds, cityId);
        }

        List<RequestingDTO> requestLists = getRequestList(iRequestingDTOS);

        RequestingSuggestionResponse response = new RequestingSuggestionResponse();
        response.setRequestList(requestLists);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String getRepairerSuggestionTypeValidated(String type) {
        for (RepairerSuggestionType t : RepairerSuggestionType.values()) {
            if (t.name().equals(type)) {
                return type;
            }
        }
        throw new GeneralException(HttpStatus.GONE, INVALID_REPAIRER_SUGGESTION_TYPE);
    }


    @Override
    public ResponseEntity<RequestingFilterResponse> getFilterRequestList(RequestingFilterRequest request) {
        List<Long> serviceIds = request.getServiceIds();
        if (serviceIds == null || serviceIds.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, SERVICE_IDS_ARE_REQUIRED);
        }

        LocalDateTime start = getFilterDateValidated(request.getStartDate()).atTime(LocalTime.MIN);
        LocalDateTime end = getFilterDateValidated(request.getEndDate()).atTime(LocalTime.MAX);

        String locationType = getLocationTypeValidated(request.getLocationType());
        String locationId = request.getLocationId();
        List<IRequestingDTO> iRequestingDTOS;
        switch (LocationType.valueOf(locationType)) {
            case CITY:
                iRequestingDTOS = repairRequestDAO.filterPendingRequestByCity(serviceIds, locationId, start, end);
                break;
            case DISTRICT:
                iRequestingDTOS = repairRequestDAO.filterPendingRequestByDistrict(serviceIds, locationId, start, end);
                break;
            default:
                iRequestingDTOS = repairRequestDAO.filterPendingRequestByCommune(serviceIds, locationId, start, end);
                break;
        }

        List<RequestingDTO> requestLists = getRequestList(iRequestingDTOS);
        RequestingFilterResponse response = new RequestingFilterResponse();
        response.setRequestList(requestLists);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private LocalDate getFilterDateValidated(String strDate) {
        if (strDate == null) {
            throw new GeneralException(HttpStatus.GONE, START_DATE_AND_END_DATE_ARE_REQUIRED);
        }

        try {
            return DateFormatUtil.getLocalDate(strDate, DATE_PATTERN);
        } catch (DateTimeParseException e) {
            throw new GeneralException(HttpStatus.GONE, WRONG_LOCAL_DATE_FORMAT);
        }
    }

    private List<RequestingDTO> getRequestList(List<IRequestingDTO> iRequestingDTOS) {
        return iRequestingDTOS.stream()
                .map(iRequestingDTO -> {
                    RequestingDTO dto = new RequestingDTO();
                    dto.setCustomerName(iRequestingDTO.getCustomerName());
                    dto.setAvatar(iRequestingDTO.getAvatar());
                    dto.setServiceName(iRequestingDTO.getServiceName());
                    dto.setExpectFixingTime(DateFormatUtil.toString(iRequestingDTO.getExpectFixingTime(), DATE_TIME_PATTERN));
                    dto.setAddress(addressService.getAddressFormatted(iRequestingDTO.getAddressId()));
                    dto.setDescription(iRequestingDTO.getDescription());
                    dto.setRequestCode(iRequestingDTO.getRequestCode());
                    dto.setIconImage(iRequestingDTO.getIconImage());
                    dto.setCreatedAt(iRequestingDTO.getCreatedAt());
                    return dto;
                }).collect(Collectors.toList());
    }

    private String getLocationTypeValidated(String type) {
        for (LocationType t : LocationType.values()) {
            if (t.name().equals(type)) {
                return type;
            }
        }
        throw new GeneralException(HttpStatus.GONE, INVALID_LOCATION_TYPE);
    }

    @Override
    public ResponseEntity<SearchSubServicesResponse> searchSubServicesByService(SearchSubServicesRequest request) {
        String keyword = request.getKeyword();
        if (keyword == null || keyword.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_KEY_WORD);
        }

        Long serviceId = request.getServiceId();
        if (serviceId == null) {
            throw new GeneralException(HttpStatus.GONE, SERVICE_ID_IS_REQUIRED);
        }

        List<SubService> subServices = subServiceDAO.searchSubServicesByService(keyword, serviceId);
        List<SubServiceOutputDTO> subServiceOutputDTOS = subServices.stream()
                .map(subService -> {
                    SubServiceOutputDTO dto = new SubServiceOutputDTO();
                    dto.setId(subService.getId());
                    dto.setName(subService.getName());
                    dto.setPrice(subService.getPrice());
                    return dto;
                }).collect(Collectors.toList());

        SearchSubServicesResponse response = new SearchSubServicesResponse();
        response.setSubServices(subServiceOutputDTOS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SearchAccessoriesResponse> searchAccessoriesByService(SearchAccessoriesRequest request) {
        String keyword = request.getKeyword();
        if (keyword == null || keyword.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_KEY_WORD);
        }
        Long serviceId = request.getServiceId();
        if (serviceId == null) {
            throw new GeneralException(HttpStatus.GONE, SERVICE_ID_IS_REQUIRED);
        }

        List<Accessory> accessories = accessoryDAO.searchAccessoriesByService(keyword, serviceId);
        List<AccessoryOutputDTO> accessoryOutputDTOS = accessories.stream()
                .map(accessory -> {
                    AccessoryOutputDTO dto = new AccessoryOutputDTO();
                    dto.setId(accessory.getId());
                    dto.setName(accessory.getName());
                    dto.setPrice(accessory.getPrice());
                    dto.setInsuranceTime(accessory.getInsuranceTime());
                    return dto;
                }).collect(Collectors.toList());

        SearchAccessoriesResponse response = new SearchAccessoriesResponse();
        response.setAccessories(accessoryOutputDTOS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}