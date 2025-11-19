package com.derkaiser.auth.commons.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Solicitud para restablecer contraseña con token")
public class ResetPasswordRequest {

    @NotBlank(message = "El token es obligatorio")
    @Schema(description = "Token de restablecimiento de contraseña", example = "abc123def456", required = true)
    private String token;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Schema(description = "Nueva contraseña (mínimo 8 caracteres)", example = "NuevaContraseña123", required = true)
    private String newPassword;

}
