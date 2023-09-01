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

        log.info("SPRING_DATASOURCE_URL: {}", System.getenv("SPRING_DATASOURCE_URL"));
        log.info("SPRING_DATASOURCE_USERNAME: {}", System.getenv("SPRING_DATASOURCE_USERNAME"));
        log.info("SPRING_DATASOURCE_PASSWORD: {}", System.getenv("SPRING_DATASOURCE_PASSWORD"));
//        log.info("RDS_PWD: {}", System.getenv("RDS_PWD"));
    }


}


