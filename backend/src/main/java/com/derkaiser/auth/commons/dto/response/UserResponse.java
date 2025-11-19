package com.derkaiser.auth.commons.dto.response;

import com.derkaiser.auth.commons.model.entity.UserEntity;
import com.derkaiser.auth.commons.model.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Respuesta con información del usuario")
public class UserResponse {

    @JsonProperty("id")
    @Schema(description = "Identificador único del usuario", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @JsonProperty("first_name")
    @Schema(description = "Nombre del usuario", example = "Juan")
    private String firstName;

    @JsonProperty("last_name")
    @Schema(description = "Apellido del usuario", example = "Pérez")
    private String lastName;

    @JsonProperty("email")
    @Schema(description = "Correo electrónico del usuario", example = "juan.perez@ejemplo.com")
    private String email;

    @JsonProperty("role")
    @Schema(description = "Rol del usuario", implementation = UserRole.class, example = "USER")
    private UserRole role;

    @JsonProperty("cargo")
    @Schema(description = "Cargo del usuario", example = "Desarrollador")
    private String cargo;

    @JsonProperty("verificated_email")
    @Schema(description = "Indica si el email ha sido verificado", example = "true")
    private Boolean verificatedEmail;

    @JsonProperty("created_at")
    @Schema(description = "Fecha de creación del usuario", example = "2023-11-19T10:00:00")
    private LocalDateTime createdAt;

    public static UserResponse fromEntity(UserEntity userEntity) {
        return UserResponse.builder()
                .id(userEntity.getId())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .email(userEntity.getEmail())
                .role(userEntity.getRole())
                .cargo(userEntity.getCargo())
                .verificatedEmail(userEntity.getVerificatedEmail())
                .createdAt(userEntity.getCreatedAt())
                .build();
    }
}
