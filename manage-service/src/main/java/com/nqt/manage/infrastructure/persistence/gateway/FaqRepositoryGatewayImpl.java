package com.nqt.manage.infrastructure.persistence.gateway;

import com.nqt.manage.domain.model.Faq;
import com.nqt.manage.infrastructure.persistence.entity.FaqEntity;
import com.nqt.manage.infrastructure.persistence.repository.FaqJpaRepository;
import com.nqt.manage.usecase.boundary.FaqRepositoryGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FaqRepositoryGatewayImpl implements FaqRepositoryGateway {

    private final FaqJpaRepository repository;

    @Override
    public Faq save(Faq domain) {
        FaqEntity entity = toEntity(domain);
        FaqEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Faq> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Faq> findAll() {
        return repository.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Faq> findActiveFaqs() {
        return repository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Faq> findByCategory(String category) {
        return repository.findByIsActiveTrueAndCategoryOrderByDisplayOrderAsc(category).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private Faq toDomain(FaqEntity entity) {
        return Faq.builder()
                .id(entity.getId())
                .question(entity.getQuestion())
                .answer(entity.getAnswer())
                .category(entity.getCategory())
                .displayOrder(entity.getDisplayOrder())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private FaqEntity toEntity(Faq domain) {
        return FaqEntity.builder()
                .id(domain.getId())
                .question(domain.getQuestion())
                .answer(domain.getAnswer())
                .category(domain.getCategory())
                .displayOrder(domain.getDisplayOrder())
                .isActive(domain.getIsActive())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
