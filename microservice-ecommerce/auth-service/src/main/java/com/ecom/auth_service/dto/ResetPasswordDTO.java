package com.ecom.auth_service.dto;

import lombok.Data;

@Data
public class ResetPasswordDTO {
    private String token_reset_password;
    private String new_password;
}
