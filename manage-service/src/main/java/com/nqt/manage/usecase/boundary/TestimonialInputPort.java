package com.nqt.manage.usecase.boundary;

import com.nqt.manage.usecase.dto.TestimonialRequestDTO;
import com.nqt.manage.usecase.dto.TestimonialResponseDTO;

import java.util.List;

public interface TestimonialInputPort {
    TestimonialResponseDTO createTestimonial(TestimonialRequestDTO requestDTO);
    TestimonialResponseDTO updateTestimonial(Long id, TestimonialRequestDTO requestDTO);
    TestimonialResponseDTO getTestimonialById(Long id);
    List<TestimonialResponseDTO> getAllTestimonials();
    List<TestimonialResponseDTO> getActiveTestimonials();
    void deleteTestimonial(Long id);
}
