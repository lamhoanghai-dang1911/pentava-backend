package com.nqt.manage.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "landing_newsletters")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsletterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String source;

    @Builder.Default
    private Boolean isSubscribed = true;

    private LocalDateTime subscribedAt;
}
