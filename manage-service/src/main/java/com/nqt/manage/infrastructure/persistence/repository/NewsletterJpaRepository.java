package com.nqt.manage.infrastructure.persistence.repository;

import com.nqt.manage.infrastructure.persistence.entity.NewsletterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsletterJpaRepository extends JpaRepository<NewsletterEntity, Long> {
    Optional<NewsletterEntity> findByEmail(String email);
}
