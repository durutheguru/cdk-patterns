package com.julianduru.learning.crud_ec2.controller;

import com.julianduru.learning.crud_ec2.dto.BusinessDto;
import com.julianduru.learning.crud_ec2.entity.Business;
import com.julianduru.learning.crud_ec2.repo.BusinessRepository;
import com.julianduru.learning.crud_ec2.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * created by Julian Dumebi Duru on 02/09/2023
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(BusinessController.PATH)
public class BusinessController {

    public static final String PATH = "/api/business";

    private final UserRepository userRepository;

    private final BusinessRepository businessRepository;


    @PostMapping
    public Business save(@Validated @RequestBody BusinessDto businessDto) {
        var user = userRepository.findByUsername(businessDto.getUsername())
            .orElseThrow(() -> new RuntimeException("User does not exist. Please fix this"));

        var business = new Business();
        business.setName(businessDto.getName());
        business.setUser(user);

        return businessRepository.save(business);
    }


    @GetMapping
    public List<Business> getBusinesses() {
        return businessRepository.findAll();
    }


}


