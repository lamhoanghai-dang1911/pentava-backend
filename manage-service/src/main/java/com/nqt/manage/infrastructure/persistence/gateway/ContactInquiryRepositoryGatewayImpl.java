package com.nqt.manage.infrastructure.persistence.gateway;

import com.nqt.manage.domain.model.ContactInquiry;
import com.nqt.manage.domain.model.ContactStatus;
import com.nqt.manage.infrastructure.persistence.entity.ContactInquiryEntity;
import com.nqt.manage.infrastructure.persistence.repository.ContactInquiryJpaRepository;
import com.nqt.manage.usecase.boundary.ContactInquiryRepositoryGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ContactInquiryRepositoryGatewayImpl implements ContactInquiryRepositoryGateway {

    private final ContactInquiryJpaRepository repository;

    @Override
    public ContactInquiry save(ContactInquiry domain) {
        ContactInquiryEntity entity = toEntity(domain);
        ContactInquiryEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<ContactInquiry> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<ContactInquiry> findAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ContactInquiry> findByStatus(ContactStatus status) {
        return repository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private ContactInquiry toDomain(ContactInquiryEntity entity) {
        return ContactInquiry.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .subject(entity.getSubject())
                .message(entity.getMessage())
                .status(entity.getStatus())
                .adminNote(entity.getAdminNote())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private ContactInquiryEntity toEntity(ContactInquiry domain) {
        return ContactInquiryEntity.builder()
                .id(domain.getId())
                .fullName(domain.getFullName())
                .email(domain.getEmail())
                .phone(domain.getPhone())
                .subject(domain.getSubject())
                .message(domain.getMessage())
                .status(domain.getStatus())
                .adminNote(domain.getAdminNote())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
