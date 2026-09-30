package com.nqt.manage.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Newsletter {
    private Long id;
    private String email;
    private String source;
    private Boolean isSubscribed;
    private LocalDateTime subscribedAt;
}
