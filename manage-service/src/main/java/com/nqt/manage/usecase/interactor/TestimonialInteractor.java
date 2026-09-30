package com.nqt.manage.usecase.interactor;

import com.nqt.manage.domain.exception.ResourceNotFoundException;
import com.nqt.manage.domain.model.Testimonial;
import com.nqt.manage.usecase.boundary.OnboardingServiceGateway;
import com.nqt.manage.usecase.boundary.TestimonialInputPort;
import com.nqt.manage.usecase.boundary.TestimonialRepositoryGateway;
import com.nqt.manage.usecase.dto.TestimonialRequestDTO;
import com.nqt.manage.usecase.dto.TestimonialResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestimonialInteractor implements TestimonialInputPort {

    private final TestimonialRepositoryGateway testimonialRepositoryGateway;
    private final OnboardingServiceGateway onboardingServiceGateway;

    @Override
    public TestimonialResponseDTO createTestimonial(TestimonialRequestDTO requestDTO) {
        LocalDateTime now = LocalDateTime.now();

        // Nếu có truyền userId, thử đồng bộ tên mới nhất từ onboarding-service
        String authorName = requestDTO.getAuthorName();
        if (requestDTO.getUserId() != null && requestDTO.getUserId() > 0) {
            authorName = onboardingServiceGateway.getProfileNameByUserId(requestDTO.getUserId())
                    .filter(name -> !name.isBlank())
                    .orElse(authorName);
        }

        Testimonial testimonial = Testimonial.builder()
                .authorName(authorName)
                .authorTitle(requestDTO.getAuthorTitle())
                .avatarUrl(requestDTO.getAvatarUrl())
                .content(requestDTO.getContent())
                .rating(requestDTO.getRating() != null ? requestDTO.getRating() : 5)
                .userId(requestDTO.getUserId())
                .isFeatured(requestDTO.getIsFeatured() != null ? requestDTO.getIsFeatured() : false)
                .displayOrder(requestDTO.getDisplayOrder() != null ? requestDTO.getDisplayOrder() : 0)
                .isActive(requestDTO.getIsActive() != null ? requestDTO.getIsActive() : true)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return toDTO(testimonialRepositoryGateway.save(testimonial));
    }

    @Override
    public TestimonialResponseDTO updateTestimonial(Long id, TestimonialRequestDTO requestDTO) {
        Testimonial testimonial = testimonialRepositoryGateway.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá với ID: " + id));

        String authorName = requestDTO.getAuthorName();
        if (requestDTO.getUserId() != null && requestDTO.getUserId() > 0) {
            authorName = onboardingServiceGateway.getProfileNameByUserId(requestDTO.getUserId())
                    .filter(name -> !name.isBlank())
                    .orElse(authorName);
        }

        testimonial.setAuthorName(authorName);
        testimonial.setAuthorTitle(requestDTO.getAuthorTitle());
        testimonial.setAvatarUrl(requestDTO.getAvatarUrl());
        testimonial.setContent(requestDTO.getContent());
        if (requestDTO.getRating() != null) {
            testimonial.setRating(requestDTO.getRating());
        }
        testimonial.setUserId(requestDTO.getUserId());
        if (requestDTO.getIsFeatured() != null) {
            testimonial.setIsFeatured(requestDTO.getIsFeatured());
        }
        if (requestDTO.getDisplayOrder() != null) {
            testimonial.setDisplayOrder(requestDTO.getDisplayOrder());
        }
        if (requestDTO.getIsActive() != null) {
            testimonial.setIsActive(requestDTO.getIsActive());
        }
        testimonial.setUpdatedAt(LocalDateTime.now());

        return toDTO(testimonialRepositoryGateway.save(testimonial));
    }

    @Override
    public TestimonialResponseDTO getTestimonialById(Long id) {
        return testimonialRepositoryGateway.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá với ID: " + id));
    }

    @Override
    public List<TestimonialResponseDTO> getAllTestimonials() {
        return testimonialRepositoryGateway.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TestimonialResponseDTO> getActiveTestimonials() {
        return testimonialRepositoryGateway.findActiveTestimonials().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteTestimonial(Long id) {
        if (testimonialRepositoryGateway.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy đánh giá với ID: " + id);
        }
        testimonialRepositoryGateway.deleteById(id);
    }

    private TestimonialResponseDTO toDTO(Testimonial t) {
        return TestimonialResponseDTO.builder()
                .id(t.getId())
                .authorName(t.getAuthorName())
                .authorTitle(t.getAuthorTitle())
                .avatarUrl(t.getAvatarUrl())
                .content(t.getContent())
                .rating(t.getRating())
                .userId(t.getUserId())
                .isFeatured(t.getIsFeatured())
                .displayOrder(t.getDisplayOrder())
                .isActive(t.getIsActive())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
