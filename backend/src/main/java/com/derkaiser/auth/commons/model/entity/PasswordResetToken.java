package com.derkaiser.auth.commons.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity userEntity;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Helper method
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    // Constructor helper
    public static PasswordResetToken create(UserEntity userEntity) {
        return PasswordResetToken.builder()
                .token(UUID.randomUUID().toString())
                .userEntity(userEntity)
                .expiryDate(LocalDateTime.now().plusHours(1))  // 1 hora para reset
                .createdAt(LocalDateTime.now())
                .build();
    }


}
