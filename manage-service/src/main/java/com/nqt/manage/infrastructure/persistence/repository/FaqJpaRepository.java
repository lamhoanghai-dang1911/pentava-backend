package com.nqt.manage.infrastructure.persistence.repository;

import com.nqt.manage.infrastructure.persistence.entity.FaqEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaqJpaRepository extends JpaRepository<FaqEntity, Long> {
    List<FaqEntity> findByIsActiveTrueOrderByDisplayOrderAsc();
    List<FaqEntity> findByIsActiveTrueAndCategoryOrderByDisplayOrderAsc(String category);
    List<FaqEntity> findAllByOrderByDisplayOrderAsc();
}
