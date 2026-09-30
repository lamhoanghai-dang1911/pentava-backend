package com.nqt.manage.usecase.boundary;

import com.nqt.manage.domain.model.ContactStatus;
import com.nqt.manage.usecase.dto.ContactInquiryResponseDTO;
import com.nqt.manage.usecase.dto.CreateContactRequestDTO;
import com.nqt.manage.usecase.dto.UpdateContactStatusRequestDTO;

import java.util.List;

public interface ContactInquiryInputPort {
    ContactInquiryResponseDTO submitContact(CreateContactRequestDTO requestDTO);
    ContactInquiryResponseDTO updateStatus(Long id, UpdateContactStatusRequestDTO requestDTO);
    ContactInquiryResponseDTO getContactById(Long id);
    List<ContactInquiryResponseDTO> getAllContacts();
    List<ContactInquiryResponseDTO> getContactsByStatus(ContactStatus status);
    void deleteContact(Long id);
}
