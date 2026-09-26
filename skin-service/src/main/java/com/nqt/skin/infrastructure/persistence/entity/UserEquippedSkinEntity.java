package com.nqt.skin.infrastructure.persistence.entity;

import com.nqt.skin.domain.model.Slot;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_equipped_skin", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "slot"})
}, indexes = {
        @Index(name = "idx_user_equipped_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEquippedSkinEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Slot slot;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "skin_item_id", nullable = false)
    private SkinItemEntity skinItem;

    @Builder.Default
    @Column(name = "equipped_at", nullable = false)
    private LocalDateTime equippedAt = LocalDateTime.now();
}
