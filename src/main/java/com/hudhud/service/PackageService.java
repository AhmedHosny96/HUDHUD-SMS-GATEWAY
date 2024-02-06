package com.hudhud.service;


import com.hudhud.exception.CustomException;
import com.hudhud.model.Packages;
import com.hudhud.model.dto.CustomResponse;
import com.hudhud.repository.PackageRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor


public class PackageService {

    private final PackageRepository packageRepository;

    public List<Packages> getAllPackages() {
        return packageRepository.findAll();
    }

    public Packages createPackage(Packages packageRequest) throws CustomException {

        Optional<Packages> byName = packageRepository.findByName(packageRequest.getName());

        Optional<Packages> byType = packageRepository.findByType(packageRequest.getType());

        if (byName.isPresent()) {
            throw new CustomException("Duplicate package name");
        }
//        if (byType.isPresent()) {
//            throw new CustomException("Duplicate package type");
//        }

        return packageRepository.save(packageRequest);
    }

    public CustomResponse updatePackage(int id, Packages packageRequest) throws CustomException {
        return packageRepository.findById(id)
                .map(existingPackage -> {
                    existingPackage.setName(packageRequest.getName());
                    existingPackage.setType(packageRequest.getType());
                    existingPackage.setNumber(packageRequest.getNumber());
                    packageRepository.save(existingPackage);
                    var response = new CustomResponse();
                    response.setStatus("200");
                    response.setMessage("Package updated successfully");
                    return response;
                })
                .orElseThrow(() -> new CustomException("Package not found"));
    }
}
