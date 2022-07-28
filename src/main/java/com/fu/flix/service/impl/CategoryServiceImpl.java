package com.fu.flix.service.impl;

import com.fu.flix.constant.Constant;
import com.fu.flix.dao.AccessoryDAO;
import com.fu.flix.dao.ImageDAO;
import com.fu.flix.dao.ServiceDAO;
import com.fu.flix.dao.SubServiceDAO;
import com.fu.flix.dto.*;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.AccessoriesResponse;
import com.fu.flix.dto.response.SearchActiveServicesResponse;
import com.fu.flix.dto.response.SubServiceResponse;
import com.fu.flix.entity.Accessory;
import com.fu.flix.entity.Image;
import com.fu.flix.entity.SubService;
import com.fu.flix.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fu.flix.constant.Constant.INVALID_KEY_WORD;
import static com.fu.flix.constant.Constant.INVALID_SERVICE;
import static com.fu.flix.constant.enums.ActiveState.ACTIVE;
import static com.fu.flix.constant.enums.ActiveState.INACTIVE;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final ServiceDAO serviceDAO;
    private final ImageDAO imageDAO;
    private final SubServiceDAO subServiceDAO;
    private final AccessoryDAO accessoryDAO;

    public CategoryServiceImpl(ServiceDAO serviceDAO,
                               ImageDAO imageDAO,
                               SubServiceDAO subServiceDAO,
                               AccessoryDAO accessoryDAO) {
        this.serviceDAO = serviceDAO;
        this.imageDAO = imageDAO;
        this.subServiceDAO = subServiceDAO;
        this.accessoryDAO = accessoryDAO;
    }

    @Override
    public ResponseEntity<ServiceResponse> getServicesByCategory(ServiceRequest request) {
        Long categoryId = request.getCategoryId();
        if (categoryId == null) {
            throw new GeneralException(HttpStatus.GONE, Constant.INVALID_CATEGORY_ID);
        }

        List<com.fu.flix.entity.Service> services = serviceDAO.findByCategoryIdAndIsActive(categoryId, true);
        if (services.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, Constant.INVALID_CATEGORY_ID);
        }

        List<ServiceDTO> serviceDTOS = services.stream()
                .map(this::mapToServiceDTO)
                .collect(Collectors.toList());

        ServiceResponse response = new ServiceResponse();
        response.setServices(serviceDTOS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ServiceDTO mapToServiceDTO(com.fu.flix.entity.Service service) {
        Optional<Image> optionalImage = imageDAO.findById(service.getImageId());
        Optional<Image> optionalIcon = imageDAO.findById(service.getIconId());

        ServiceDTO dto = new ServiceDTO();
        dto.setId(service.getId());
        dto.setPrice(service.getInspectionPrice());
        dto.setImage(optionalImage.map(Image::getUrl).orElse(null));
        dto.setServiceName(service.getName());
        dto.setIcon(optionalIcon.map(Image::getUrl).orElse(null));
        dto.setStatus(service.isActive() ? ACTIVE.name() : INACTIVE.name());
        dto.setDescription(service.getDescription());
        return dto;
    }

    @Override
    public ResponseEntity<SearchActiveServicesResponse> searchServices(SearchActiveServicesRequest request) {
        String keyword = request.getKeyword();
        if (keyword == null || keyword.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_KEY_WORD);
        }

        List<ISearchActiveServiceDTO> services = serviceDAO.searchActiveServices(keyword);

        List<SearchServiceDTO> searchServiceDTOS = services.stream()
                .map(service -> {
                    SearchServiceDTO dto = new SearchServiceDTO();
                    dto.setServiceId(service.getServiceId());
                    dto.setServiceName(service.getServiceName());
                    dto.setIcon(service.getIcon());
                    dto.setImage(service.getImage());
                    dto.setStatus(service.getStatus());
                    dto.setPrice(service.getPrice());
                    return dto;
                }).collect(Collectors.toList());

        SearchActiveServicesResponse response = new SearchActiveServicesResponse();
        response.setServices(searchServiceDTOS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SubServiceResponse> getSubServicesByServiceId(SubServiceRequest request) {
        Long serviceId = request.getServiceId();
        if (serviceId == null) {
            throw new GeneralException(HttpStatus.GONE, INVALID_SERVICE);
        }

        List<SubService> subServices = subServiceDAO.findByServiceIdAndIsActive(serviceId, true);
        List<SubServiceOutputDTO> subServiceDTOS = subServices.stream()
                .map(subService -> {
                    SubServiceOutputDTO dto = new SubServiceOutputDTO();
                    dto.setId(subService.getId());
                    dto.setName(subService.getName());
                    dto.setPrice(subService.getPrice());
                    return dto;
                }).collect(Collectors.toList());

        SubServiceResponse response = new SubServiceResponse();
        response.setSubServices(subServiceDTOS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AccessoriesResponse> getAccessoriesByServiceId(AccessoriesRequest request) {
        Long serviceId = request.getServiceId();
        if (serviceId == null) {
            throw new GeneralException(HttpStatus.GONE, INVALID_SERVICE);
        }

        List<Accessory> accessories = accessoryDAO.findByServiceId(serviceId);
        List<AccessoryOutputDTO> accessoryDTOS = accessories.stream()
                .map(accessory -> {
                    AccessoryOutputDTO dto = new AccessoryOutputDTO();
                    dto.setId(accessory.getId());
                    dto.setName(accessory.getName());
                    dto.setPrice(accessory.getPrice());
                    dto.setInsuranceTime(accessory.getInsuranceTime());
                    return dto;
                }).collect(Collectors.toList());

        AccessoriesResponse response = new AccessoriesResponse();
        response.setAccessories(accessoryDTOS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
