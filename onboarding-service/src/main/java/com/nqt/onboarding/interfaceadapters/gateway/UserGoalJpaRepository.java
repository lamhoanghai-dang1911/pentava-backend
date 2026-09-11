package com.nqt.onboarding.interfaceadapters.gateway;
//package com.nqt.onboarding.interfaceadapters.gateway;
import com.nqt.onboarding.infrastructure.persistence.UserGoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserGoalJpaRepository extends JpaRepository<UserGoalEntity, Long> {
    Optional<UserGoalEntity> findByUserEmailAndGoalName(String userEmail, String goalName);
}
