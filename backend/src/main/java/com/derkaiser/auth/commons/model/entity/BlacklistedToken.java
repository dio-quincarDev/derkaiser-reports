package com.derkaiser.auth.commons.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "blacklisted_tokens")
public class BlacklistedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType tokenType;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    public static BlacklistedToken create(String token, TokenType tokenType, LocalDateTime expiryDate) {
        return BlacklistedToken.builder()
                .token(token)
                .tokenType(tokenType)
                .expiryDate(expiryDate)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public enum TokenType {
        ACCESS,
        REFRESH
    }
}