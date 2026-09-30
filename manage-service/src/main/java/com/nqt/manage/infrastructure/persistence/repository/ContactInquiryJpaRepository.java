package com.nqt.manage.infrastructure.persistence.repository;

import com.nqt.manage.domain.model.ContactStatus;
import com.nqt.manage.infrastructure.persistence.entity.ContactInquiryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactInquiryJpaRepository extends JpaRepository<ContactInquiryEntity, Long> {
    List<ContactInquiryEntity> findByStatusOrderByCreatedAtDesc(ContactStatus status);
    List<ContactInquiryEntity> findAllByOrderByCreatedAtDesc();
}
