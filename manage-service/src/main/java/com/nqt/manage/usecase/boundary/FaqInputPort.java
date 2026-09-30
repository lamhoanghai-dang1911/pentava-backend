package com.nqt.manage.usecase.boundary;

import com.nqt.manage.usecase.dto.FaqRequestDTO;
import com.nqt.manage.usecase.dto.FaqResponseDTO;

import java.util.List;

public interface FaqInputPort {
    FaqResponseDTO createFaq(FaqRequestDTO requestDTO);
    FaqResponseDTO updateFaq(Long id, FaqRequestDTO requestDTO);
    FaqResponseDTO getFaqById(Long id);
    List<FaqResponseDTO> getAllFaqs();
    List<FaqResponseDTO> getActiveFaqs();
    List<FaqResponseDTO> getFaqsByCategory(String category);
    void deleteFaq(Long id);
}
