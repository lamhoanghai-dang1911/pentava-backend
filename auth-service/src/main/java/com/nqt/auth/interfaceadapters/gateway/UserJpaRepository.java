package com.nqt.auth.interfaceadapters.gateway;

import com.nqt.auth.infrastructure.persistence.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByResetToken(String resetToken);

    @org.springframework.data.jpa.repository.Query("""
        SELECT u FROM UserEntity u
        WHERE u.id NOT IN :excludedIds
        ORDER BY u.id DESC
    """)
    java.util.List<UserEntity> findSuggestedUsers(
            @org.springframework.data.repository.query.Param("excludedIds") java.util.List<Long> excludedIds,
            org.springframework.data.domain.Pageable pageable
    );

    @org.springframework.data.jpa.repository.Query("""
        SELECT u FROM UserEntity u
        WHERE (:search IS NULL OR TRIM(:search) = '' 
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
               OR (u.name IS NOT NULL AND LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%'))))
        ORDER BY u.id DESC
    """)
    java.util.List<UserEntity> findAllBySearch(@org.springframework.data.repository.query.Param("search") String search);
}