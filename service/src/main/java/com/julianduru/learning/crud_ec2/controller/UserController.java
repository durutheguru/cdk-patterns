package com.julianduru.learning.crud_ec2.controller;

import com.julianduru.learning.crud_ec2.dto.UserDto;
import com.julianduru.learning.crud_ec2.entity.User;
import com.julianduru.learning.crud_ec2.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * created by Julian Dumebi Duru on 16/07/2023
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(UserController.PATH)
public class UserController {

    public static final String PATH = "/api/user";

    private final UserRepository userRepository;



    @PostMapping
    public User save(@Validated @RequestBody UserDto user) {
        var usr = new User();
        usr.setUsername(user.getUsername());
        usr.setFirstName(user.getFirstName());
        usr.setLastName(user.getLastName());

        return this.userRepository.save(usr);
    }


    @GetMapping
    public List<User> getUsers() {
        return userRepository.findAll();
    }


}


