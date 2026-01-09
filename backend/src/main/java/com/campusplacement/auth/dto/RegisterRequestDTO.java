package com.campusplacement.auth.dto;

import com.campusplacement.common.Constants;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user registration request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = Constants.Validation.MIN_PASSWORD_LENGTH, max = Constants.Validation.MAX_PASSWORD_LENGTH, message = "Password must be between 8 and 100 characters")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

}
