package com.nqt.manage.usecase.boundary;

import com.nqt.manage.usecase.dto.NewsletterResponseDTO;
import com.nqt.manage.usecase.dto.SubscribeNewsletterRequestDTO;

import java.util.List;

public interface NewsletterInputPort {
    NewsletterResponseDTO subscribe(SubscribeNewsletterRequestDTO requestDTO);
    List<NewsletterResponseDTO> getAllSubscriptions();
    void unsubscribe(Long id);
}
