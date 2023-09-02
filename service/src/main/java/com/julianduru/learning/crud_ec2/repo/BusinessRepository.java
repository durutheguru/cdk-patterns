package com.julianduru.learning.crud_ec2.repo;

import com.julianduru.learning.crud_ec2.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * created by Julian Dumebi Duru on 02/09/2023
 */
@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {



}
