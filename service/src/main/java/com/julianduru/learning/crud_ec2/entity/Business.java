package com.julianduru.learning.crud_ec2.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * created by Julian Dumebi Duru on 02/09/2023
 */
@Data
@Entity
public class Business {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    @ManyToOne
    @JoinColumn(nullable = false)
    private User user;


    @Column(nullable = false, length = 200)
    private String name;


}

