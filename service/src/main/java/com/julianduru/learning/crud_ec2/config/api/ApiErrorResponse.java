package com.julianduru.learning.crud_ec2.config.api;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;

/**
 * created by julian
 */
public class ApiErrorResponse extends ApiResponse<String> {

    public Long code;


    @JsonIgnore
    private Exception exception;


    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm")
    public LocalDateTime timeStamp;


    public ApiErrorResponse() {
        super(ApiStatus.ERROR, null);
        initialize();
    }


    public ApiErrorResponse(String message) {
        super(ApiStatus.ERROR, message);
        initialize();
    }


    public ApiErrorResponse(String message, String data) {
        super(ApiStatus.ERROR, message, data);
        initialize();
    }


    public ApiErrorResponse(Exception exception) {
        super(ApiStatus.ERROR, ApiBodySanitizer.sanitizeMessage(exception));
        this.exception = exception;
        initialize();
    }


    private void initialize() {
        this.code = System.currentTimeMillis() % 100000;
        timeStamp = LocalDateTime.now();
    }


}

