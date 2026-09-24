package com.nqt.social.usecase.boundary;

import com.nqt.social.domain.SocialEvent;

public interface SocialEventPublisherGateway {
    void publish(SocialEvent event);
}
