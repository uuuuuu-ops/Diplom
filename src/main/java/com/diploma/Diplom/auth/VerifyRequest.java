package com.diploma.Diplom.auth;

import lombok.Data;

@Data
public class VerifyRequest {

    private String email;
    private String code;

}