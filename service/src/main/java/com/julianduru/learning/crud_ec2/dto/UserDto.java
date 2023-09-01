package com.julianduru.learning.crud_ec2.dto;

import jakarta.validation.constraints.NotEmpty;

/**
 * created by Julian Dumebi Duru on 16/07/2023
 */
public class UserDto {


   @NotEmpty(message = "Username is required")
   private String username;


   @NotEmpty(message = "First Name is required")
   private String firstName;


   @NotEmpty(message = "Last Name is required")
   private String lastName;


   public String getUsername() {
      return username;
   }


   public UserDto setUsername(String username) {
      this.username = username;
      return this;
   }


   public String getFirstName() {
      return firstName;
   }

   public UserDto setFirstName(String firstName) {
      this.firstName = firstName;
      return this;
   }

   public String getLastName() {
      return lastName;
   }

   public UserDto setLastName(String lastName) {
      this.lastName = lastName;
      return this;
   }


}
