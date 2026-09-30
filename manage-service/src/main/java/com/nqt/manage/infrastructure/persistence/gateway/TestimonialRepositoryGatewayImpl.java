package com.nqt.manage.infrastructure.persistence.gateway;

import com.nqt.manage.domain.model.Testimonial;
import com.nqt.manage.infrastructure.persistence.entity.TestimonialEntity;
import com.nqt.manage.infrastructure.persistence.repository.TestimonialJpaRepository;
import com.nqt.manage.usecase.boundary.TestimonialRepositoryGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TestimonialRepositoryGatewayImpl implements TestimonialRepositoryGateway {

    private final TestimonialJpaRepository repository;

    @Override
    public Testimonial save(Testimonial domain) {
        TestimonialEntity entity = toEntity(domain);
        TestimonialEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Testimonial> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Testimonial> findAll() {
        return repository.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Testimonial> findActiveTestimonials() {
        return repository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private Testimonial toDomain(TestimonialEntity entity) {
        return Testimonial.builder()
                .id(entity.getId())
                .authorName(entity.getAuthorName())
                .authorTitle(entity.getAuthorTitle())
                .avatarUrl(entity.getAvatarUrl())
                .content(entity.getContent())
                .rating(entity.getRating())
                .userId(entity.getUserId())
                .isFeatured(entity.getIsFeatured())
                .displayOrder(entity.getDisplayOrder())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private TestimonialEntity toEntity(Testimonial domain) {
        return TestimonialEntity.builder()
                .id(domain.getId())
                .authorName(domain.getAuthorName())
                .authorTitle(domain.getAuthorTitle())
                .avatarUrl(domain.getAvatarUrl())
                .content(domain.getContent())
                .rating(domain.getRating())
                .userId(domain.getUserId())
                .isFeatured(domain.getIsFeatured())
                .displayOrder(domain.getDisplayOrder())
                .isActive(domain.getIsActive())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
