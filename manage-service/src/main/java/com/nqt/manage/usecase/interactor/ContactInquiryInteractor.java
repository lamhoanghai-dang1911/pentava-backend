package com.nqt.manage.usecase.interactor;

import com.nqt.manage.domain.exception.ResourceNotFoundException;
import com.nqt.manage.domain.model.ContactInquiry;
import com.nqt.manage.domain.model.ContactStatus;
import com.nqt.manage.usecase.boundary.ContactInquiryInputPort;
import com.nqt.manage.usecase.boundary.ContactInquiryRepositoryGateway;
import com.nqt.manage.usecase.dto.ContactInquiryResponseDTO;
import com.nqt.manage.usecase.dto.CreateContactRequestDTO;
import com.nqt.manage.usecase.dto.UpdateContactStatusRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactInquiryInteractor implements ContactInquiryInputPort {

    private final ContactInquiryRepositoryGateway contactInquiryRepositoryGateway;

    @Override
    public ContactInquiryResponseDTO submitContact(CreateContactRequestDTO requestDTO) {
        LocalDateTime now = LocalDateTime.now();
        ContactInquiry inquiry = ContactInquiry.builder()
                .fullName(requestDTO.getFullName())
                .email(requestDTO.getEmail())
                .phone(requestDTO.getPhone())
                .subject(requestDTO.getSubject())
                .message(requestDTO.getMessage())
                .status(ContactStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return toDTO(contactInquiryRepositoryGateway.save(inquiry));
    }

    @Override
    public ContactInquiryResponseDTO updateStatus(Long id, UpdateContactStatusRequestDTO requestDTO) {
        ContactInquiry inquiry = contactInquiryRepositoryGateway.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin liên hệ với ID: " + id));

        inquiry.setStatus(requestDTO.getStatus());
        if (requestDTO.getAdminNote() != null) {
            inquiry.setAdminNote(requestDTO.getAdminNote());
        }
        inquiry.setUpdatedAt(LocalDateTime.now());

        return toDTO(contactInquiryRepositoryGateway.save(inquiry));
    }

    @Override
    public ContactInquiryResponseDTO getContactById(Long id) {
        return contactInquiryRepositoryGateway.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin liên hệ với ID: " + id));
    }

    @Override
    public List<ContactInquiryResponseDTO> getAllContacts() {
        return contactInquiryRepositoryGateway.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ContactInquiryResponseDTO> getContactsByStatus(ContactStatus status) {
        return contactInquiryRepositoryGateway.findByStatus(status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteContact(Long id) {
        if (contactInquiryRepositoryGateway.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy thông tin liên hệ với ID: " + id);
        }
        contactInquiryRepositoryGateway.deleteById(id);
    }

    private ContactInquiryResponseDTO toDTO(ContactInquiry c) {
        return ContactInquiryResponseDTO.builder()
                .id(c.getId())
                .fullName(c.getFullName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .subject(c.getSubject())
                .message(c.getMessage())
                .status(c.getStatus())
                .adminNote(c.getAdminNote())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
