package com.julianduru.learning.crud_ec2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * created by Julian Dumebi Duru on 10/08/2023
 */
@Slf4j
@Component
public class ApplicationStartedHandler {


    @EventListener(ApplicationStartedEvent.class)
    public void handleApplicationStarted() {
        log.info("Application Started");

        log.info("RDS_HOSTNAME: {}", System.getenv("RDS_HOSTNAME"));
        log.info("RDS_PORT: {}", System.getenv("RDS_PORT"));
        log.info("RDS_USER: {}", System.getenv("RDS_USER"));
        log.info("RDS_PWD: {}", System.getenv("RDS_PWD"));
    }


}


