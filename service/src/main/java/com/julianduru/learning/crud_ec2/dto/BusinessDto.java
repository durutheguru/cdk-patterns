package com.julianduru.learning.crud_ec2.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * created by Julian Dumebi Duru on 02/09/2023
 */
@Data
public class BusinessDto {


    @NotEmpty(message = "Business name is required")
    private String name;


    @NotEmpty(message = "Username is required")
    private String username;


}
