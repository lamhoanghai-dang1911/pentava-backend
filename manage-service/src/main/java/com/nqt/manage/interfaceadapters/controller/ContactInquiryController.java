package com.nqt.manage.interfaceadapters.controller;

import com.nqt.manage.domain.model.ContactStatus;
import com.nqt.manage.usecase.boundary.ContactInquiryInputPort;
import com.nqt.manage.usecase.dto.ContactInquiryResponseDTO;
import com.nqt.manage.usecase.dto.CreateContactRequestDTO;
import com.nqt.manage.usecase.dto.UpdateContactStatusRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Manage - Contacts", description = "Quản lý và tiếp nhận thông tin liên hệ / góp ý từ khách hàng")
@RestController
@RequestMapping("/api/manage/contacts")
@RequiredArgsConstructor
public class ContactInquiryController {

    private final ContactInquiryInputPort contactInquiryInputPort;

    @Operation(summary = "Khách gửi form liên hệ từ Landing Page (Public)")
    @PostMapping
    public ResponseEntity<ContactInquiryResponseDTO> submitContact(@Valid @RequestBody CreateContactRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contactInquiryInputPort.submitContact(requestDTO));
    }

    @Operation(summary = "Lấy danh sách liên hệ (Admin, có thể lọc theo status)")
    @GetMapping
    public ResponseEntity<List<ContactInquiryResponseDTO>> getContacts(@RequestParam(value = "status", required = false) ContactStatus status) {
        if (status != null) {
            return ResponseEntity.ok(contactInquiryInputPort.getContactsByStatus(status));
        }
        return ResponseEntity.ok(contactInquiryInputPort.getAllContacts());
    }

    @Operation(summary = "Xem chi tiết một yêu cầu liên hệ (Admin)")
    @GetMapping("/{id}")
    public ResponseEntity<ContactInquiryResponseDTO> getContactById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(contactInquiryInputPort.getContactById(id));
    }

    @Operation(summary = "Cập nhật trạng thái và ghi chú xử lý liên hệ (Admin)")
    @PutMapping("/{id}/status")
    public ResponseEntity<ContactInquiryResponseDTO> updateStatus(@PathVariable("id") Long id,
                                                                  @Valid @RequestBody UpdateContactStatusRequestDTO requestDTO) {
        return ResponseEntity.ok(contactInquiryInputPort.updateStatus(id, requestDTO));
    }

    @Operation(summary = "Xóa thông tin liên hệ (Admin)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable("id") Long id) {
        contactInquiryInputPort.deleteContact(id);
        return ResponseEntity.noContent().build();
    }
}
