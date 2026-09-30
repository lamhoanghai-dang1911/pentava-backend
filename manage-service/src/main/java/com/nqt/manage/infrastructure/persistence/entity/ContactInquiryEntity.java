package com.nqt.manage.infrastructure.persistence.entity;

import com.nqt.manage.domain.model.ContactStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "landing_contact_inquiries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactInquiryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    private String phone;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ContactStatus status = ContactStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String adminNote;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
