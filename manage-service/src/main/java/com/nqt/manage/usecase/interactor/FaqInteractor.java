package com.nqt.manage.usecase.interactor;

import com.nqt.manage.domain.exception.ResourceNotFoundException;
import com.nqt.manage.domain.model.Faq;
import com.nqt.manage.usecase.boundary.FaqInputPort;
import com.nqt.manage.usecase.boundary.FaqRepositoryGateway;
import com.nqt.manage.usecase.dto.FaqRequestDTO;
import com.nqt.manage.usecase.dto.FaqResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FaqInteractor implements FaqInputPort {

    private final FaqRepositoryGateway faqRepositoryGateway;

    @Override
    public FaqResponseDTO createFaq(FaqRequestDTO requestDTO) {
        LocalDateTime now = LocalDateTime.now();
        Faq faq = Faq.builder()
                .question(requestDTO.getQuestion())
                .answer(requestDTO.getAnswer())
                .category(requestDTO.getCategory() != null ? requestDTO.getCategory() : "GENERAL")
                .displayOrder(requestDTO.getDisplayOrder() != null ? requestDTO.getDisplayOrder() : 0)
                .isActive(requestDTO.getIsActive() != null ? requestDTO.getIsActive() : true)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return toDTO(faqRepositoryGateway.save(faq));
    }

    @Override
    public FaqResponseDTO updateFaq(Long id, FaqRequestDTO requestDTO) {
        Faq faq = faqRepositoryGateway.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy câu hỏi FAQ với ID: " + id));

        faq.setQuestion(requestDTO.getQuestion());
        faq.setAnswer(requestDTO.getAnswer());
        if (requestDTO.getCategory() != null) {
            faq.setCategory(requestDTO.getCategory());
        }
        if (requestDTO.getDisplayOrder() != null) {
            faq.setDisplayOrder(requestDTO.getDisplayOrder());
        }
        if (requestDTO.getIsActive() != null) {
            faq.setIsActive(requestDTO.getIsActive());
        }
        faq.setUpdatedAt(LocalDateTime.now());

        return toDTO(faqRepositoryGateway.save(faq));
    }

    @Override
    public FaqResponseDTO getFaqById(Long id) {
        return faqRepositoryGateway.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy câu hỏi FAQ với ID: " + id));
    }

    @Override
    public List<FaqResponseDTO> getAllFaqs() {
        return faqRepositoryGateway.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FaqResponseDTO> getActiveFaqs() {
        return faqRepositoryGateway.findActiveFaqs().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FaqResponseDTO> getFaqsByCategory(String category) {
        return faqRepositoryGateway.findByCategory(category).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteFaq(Long id) {
        if (faqRepositoryGateway.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy câu hỏi FAQ với ID: " + id);
        }
        faqRepositoryGateway.deleteById(id);
    }

    private FaqResponseDTO toDTO(Faq f) {
        return FaqResponseDTO.builder()
                .id(f.getId())
                .question(f.getQuestion())
                .answer(f.getAnswer())
                .category(f.getCategory())
                .displayOrder(f.getDisplayOrder())
                .isActive(f.getIsActive())
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }
}
