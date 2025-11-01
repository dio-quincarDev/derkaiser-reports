package com.derkaiser.auth.commons.dto.response;

import com.derkaiser.auth.commons.model.entity.UserEntity;
import com.derkaiser.auth.commons.model.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class UserResponse {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("role")
    private UserRole role;

    @JsonProperty("cargo")
    private String cargo;

    @JsonProperty("verificated_email")
    private Boolean verificatedEmail;

    @JsonProperty("created_at")
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
