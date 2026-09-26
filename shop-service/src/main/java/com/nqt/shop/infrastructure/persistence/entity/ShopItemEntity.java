package com.nqt.shop.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "shop_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = "code"),
        @UniqueConstraint(columnNames = "skin_item_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "skin_item_id", nullable = false)
    private Long skinItemId;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 50)
    private String slot;

    @Column(name = "price_ruby", nullable = false)
    private int priceRuby;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(length = 500)
    private String description;

    @Builder.Default
    @Column(name = "is_for_sale", nullable = false)
    private boolean isForSale = true;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
