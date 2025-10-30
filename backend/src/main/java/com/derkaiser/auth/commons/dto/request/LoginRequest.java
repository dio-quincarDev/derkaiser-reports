package com.derkaiser.auth.commons.dto.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class LoginRequest {

    @Email(message = "Formato de Email Inválido")
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;


}
