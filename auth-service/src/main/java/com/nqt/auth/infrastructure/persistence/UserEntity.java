package com.nqt.auth.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String name;

    private String authProvider;

    private String otpCode;
    private LocalDateTime otpExpiryTime;
    @Builder.Default
    private boolean isVerified = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}