package com.nqt.manage.infrastructure.persistence.repository;

import com.nqt.manage.infrastructure.persistence.entity.TestimonialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestimonialJpaRepository extends JpaRepository<TestimonialEntity, Long> {
    List<TestimonialEntity> findByIsActiveTrueOrderByDisplayOrderAsc();
    List<TestimonialEntity> findAllByOrderByDisplayOrderAsc();
}
