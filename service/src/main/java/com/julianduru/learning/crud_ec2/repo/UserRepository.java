package com.julianduru.learning.crud_ec2.repo;

import com.julianduru.learning.crud_ec2.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * created by Julian Dumebi Duru on 16/07/2023
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {



}
