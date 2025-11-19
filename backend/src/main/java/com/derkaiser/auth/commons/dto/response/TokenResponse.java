package com.derkaiser.auth.commons.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Respuesta de tokens de autenticación")
public class TokenResponse {

    @JsonProperty("access_token")
    @Schema(description = "Token de acceso JWT", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @JsonProperty("refresh_token")
    @Schema(description = "Token de refresco JWT", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9_refresh...")
    private String refreshToken;

    @JsonProperty("token_type")
    @Builder.Default
    @Schema(description = "Tipo de token", example = "Bearer", defaultValue = "Bearer")
    private String tokenType = "Bearer";

    @JsonProperty("expires_in")
    @Schema(description = "Tiempo de expiración en milisegundos", example = "900000")
    private Long expiresIn;  // Milisegundos

    @JsonProperty("user")
    @Schema(description = "Información del usuario autenticado")
    private UserResponse userResponse;

    @JsonProperty("message")
    @Schema(description = "Mensaje adicional en la respuesta", example = "Inicio de sesión exitoso")
    private String message;
}
