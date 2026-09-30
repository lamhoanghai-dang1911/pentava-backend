package com.nqt.manage.infrastructure.persistence.gateway;

import com.nqt.manage.domain.model.Newsletter;
import com.nqt.manage.infrastructure.persistence.entity.NewsletterEntity;
import com.nqt.manage.infrastructure.persistence.repository.NewsletterJpaRepository;
import com.nqt.manage.usecase.boundary.NewsletterRepositoryGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NewsletterRepositoryGatewayImpl implements NewsletterRepositoryGateway {

    private final NewsletterJpaRepository repository;

    @Override
    public Newsletter save(Newsletter domain) {
        NewsletterEntity entity = toEntity(domain);
        NewsletterEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Newsletter> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Newsletter> findByEmail(String email) {
        return repository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public List<Newsletter> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private Newsletter toDomain(NewsletterEntity entity) {
        return Newsletter.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .source(entity.getSource())
                .isSubscribed(entity.getIsSubscribed())
                .subscribedAt(entity.getSubscribedAt())
                .build();
    }

    private NewsletterEntity toEntity(Newsletter domain) {
        return NewsletterEntity.builder()
                .id(domain.getId())
                .email(domain.getEmail())
                .source(domain.getSource())
                .isSubscribed(domain.getIsSubscribed())
                .subscribedAt(domain.getSubscribedAt())
                .build();
    }
}
