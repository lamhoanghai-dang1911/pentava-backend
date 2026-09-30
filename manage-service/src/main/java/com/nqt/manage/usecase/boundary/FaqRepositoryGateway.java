package com.nqt.manage.usecase.boundary;

import com.nqt.manage.domain.model.Faq;

import java.util.List;
import java.util.Optional;

public interface FaqRepositoryGateway {
    Faq save(Faq faq);
    Optional<Faq> findById(Long id);
    List<Faq> findAll();
    List<Faq> findActiveFaqs();
    List<Faq> findByCategory(String category);
    void deleteById(Long id);
}
