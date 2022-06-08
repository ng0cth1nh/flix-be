package com.fu.flix.service.impl;

import com.fu.flix.dao.ImageDAO;
import com.fu.flix.dao.ServiceDAO;
import com.fu.flix.dto.ServiceDTO;
import com.fu.flix.dto.request.ServiceRequest;
import com.fu.flix.dto.request.ServiceResponse;
import com.fu.flix.entity.Image;
import com.fu.flix.service.MajorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MajorServiceImpl implements MajorService {
    private final ServiceDAO serviceDAO;
    private final ImageDAO imageDAO;

    public MajorServiceImpl(ServiceDAO serviceDAO,
                            ImageDAO imageDAO) {
        this.serviceDAO = serviceDAO;
        this.imageDAO = imageDAO;
    }

    @Override
    public ResponseEntity<ServiceResponse> getServicesByMajor(ServiceRequest request) {
        List<com.fu.flix.entity.Service> services = serviceDAO.findByMajorId(request.getMajorId());
        List<ServiceDTO> serviceDTOS = services.stream()
                .map(service -> {
                    Optional<Image> optionalImage = imageDAO.findById(service.getImageId());
                    ServiceDTO dto = new ServiceDTO();
                    dto.setServiceId(service.getId());
                    dto.setPrice(service.getInspectionPrice());
                    dto.setImageUrl(optionalImage.isPresent() ? optionalImage.get().getUrl() : null);
                    dto.setServiceName(service.getName());
                    return dto;
                }).collect(Collectors.toList());

        ServiceResponse response = new ServiceResponse();
        response.setServices(serviceDTOS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
