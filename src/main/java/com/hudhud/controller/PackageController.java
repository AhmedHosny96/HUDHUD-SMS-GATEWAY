package com.hudhud.controller;


import com.hudhud.exception.CustomException;
import com.hudhud.model.Packages;
import com.hudhud.model.dto.CustomResponse;
import com.hudhud.service.PackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PackageController {

    private final PackageService packageService;

    @GetMapping("/package")
    public ResponseEntity<?> getPackages() {
        List<Packages> allPackages = packageService.getAllPackages();
        return new ResponseEntity<>(allPackages, HttpStatus.OK);

    }

    @PostMapping("/package")
    public ResponseEntity<?> createPackage(@RequestBody Packages packageRequest) throws CustomException {
        Packages aPackage = packageService.createPackage(packageRequest);
        var customResponse = new CustomResponse();
        customResponse.setStatus("200");
        customResponse.setMessage("Package Successfully Created");
        return new ResponseEntity<>(customResponse, HttpStatus.OK);

    }

    @PutMapping("/package/{id}")
    public ResponseEntity<?> updatePackage(@PathVariable int id, @RequestBody Packages packageRequest) throws CustomException {
        CustomResponse customResponse = packageService.updatePackage(id, packageRequest);
        return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }
}
