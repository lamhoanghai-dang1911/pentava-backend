package com.nqt.manage.usecase.boundary;

import com.nqt.manage.domain.model.Newsletter;

import java.util.List;
import java.util.Optional;

public interface NewsletterRepositoryGateway {
    Newsletter save(Newsletter newsletter);
    Optional<Newsletter> findById(Long id);
    Optional<Newsletter> findByEmail(String email);
    List<Newsletter> findAll();
    void deleteById(Long id);
}
