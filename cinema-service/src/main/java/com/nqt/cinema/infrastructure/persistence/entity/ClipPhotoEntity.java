//package com.nqt.cinema.infrastructure.persistence.entity;
package com.nqt.cinema.infrastructure.persistence.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "clip_photos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClipPhotoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clip_id", nullable = false)
    private ClipEntity clip;

    @Column(nullable = false)
    private Long photoId;

    // Snapshot URL lúc tạo clip — checkin cho phép ghi đè ảnh cùng ngày nên không được tham
    // chiếu ngược lại checkin-service, phải lưu URL tại đây để clip cũ không đổi theo.
    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private LocalDate checkinDate;

    @Column(nullable = false)
    private int displayOrder;
}