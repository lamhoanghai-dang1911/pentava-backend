package com.nqt.manage.usecase.interactor;

import com.nqt.manage.domain.exception.ResourceNotFoundException;
import com.nqt.manage.domain.model.Newsletter;
import com.nqt.manage.usecase.boundary.NewsletterInputPort;
import com.nqt.manage.usecase.boundary.NewsletterRepositoryGateway;
import com.nqt.manage.usecase.dto.NewsletterResponseDTO;
import com.nqt.manage.usecase.dto.SubscribeNewsletterRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsletterInteractor implements NewsletterInputPort {

    private final NewsletterRepositoryGateway newsletterRepositoryGateway;

    @Override
    public NewsletterResponseDTO subscribe(SubscribeNewsletterRequestDTO requestDTO) {
        String email = requestDTO.getEmail().trim().toLowerCase();
        Newsletter newsletter = newsletterRepositoryGateway.findByEmail(email)
                .orElse(Newsletter.builder()
                        .email(email)
                        .source(requestDTO.getSource() != null ? requestDTO.getSource() : "LANDING_PAGE")
                        .subscribedAt(LocalDateTime.now())
                        .build());

        newsletter.setIsSubscribed(true);
        return toDTO(newsletterRepositoryGateway.save(newsletter));
    }

    @Override
    public List<NewsletterResponseDTO> getAllSubscriptions() {
        return newsletterRepositoryGateway.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void unsubscribe(Long id) {
        Newsletter newsletter = newsletterRepositoryGateway.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin đăng ký với ID: " + id));
        newsletter.setIsSubscribed(false);
        newsletterRepositoryGateway.save(newsletter);
    }

    private NewsletterResponseDTO toDTO(Newsletter n) {
        return NewsletterResponseDTO.builder()
                .id(n.getId())
                .email(n.getEmail())
                .source(n.getSource())
                .isSubscribed(n.getIsSubscribed())
                .subscribedAt(n.getSubscribedAt())
                .build();
    }
}
