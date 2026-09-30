package com.nqt.manage.usecase.boundary;

import com.nqt.manage.domain.model.Testimonial;

import java.util.List;
import java.util.Optional;

public interface TestimonialRepositoryGateway {
    Testimonial save(Testimonial testimonial);
    Optional<Testimonial> findById(Long id);
    List<Testimonial> findAll();
    List<Testimonial> findActiveTestimonials();
    void deleteById(Long id);
}
