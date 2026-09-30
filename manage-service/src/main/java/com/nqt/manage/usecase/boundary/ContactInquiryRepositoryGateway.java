package com.nqt.manage.usecase.boundary;

import com.nqt.manage.domain.model.ContactInquiry;
import com.nqt.manage.domain.model.ContactStatus;

import java.util.List;
import java.util.Optional;

public interface ContactInquiryRepositoryGateway {
    ContactInquiry save(ContactInquiry inquiry);
    Optional<ContactInquiry> findById(Long id);
    List<ContactInquiry> findAll();
    List<ContactInquiry> findByStatus(ContactStatus status);
    void deleteById(Long id);
}
