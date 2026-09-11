package com.nqt.onboarding.interfaceadapters.gateway;
//package com.nqt.onboarding.interfaceadapters.gateway;

import com.nqt.onboarding.infrastructure.persistence.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserProfileJpaRepository extends JpaRepository<UserProfileEntity, Long> {
    Optional<UserProfileEntity> findByUserEmail(String userEmail);
}
