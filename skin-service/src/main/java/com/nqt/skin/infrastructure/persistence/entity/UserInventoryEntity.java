package com.nqt.skin.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_inventory", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "skin_item_id"})
}, indexes = {
        @Index(name = "idx_user_inventory_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInventoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "skin_item_id", nullable = false)
    private SkinItemEntity skinItem;

    @Builder.Default
    @Column(name = "acquired_at", nullable = false)
    private LocalDateTime acquiredAt = LocalDateTime.now();
}
