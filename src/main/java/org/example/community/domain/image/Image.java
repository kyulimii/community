package org.example.community.domain.image;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "jpg_path")
    private String jpgPath;

    @Column(name = "webp_path")
    private String webpPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type")
    private ImageType imageType;

    @Builder
    private Image(String originalName, String jpgPath, String webpPath, ImageType imageType) {
        this.originalName = originalName;
        this.jpgPath = jpgPath;
        this.webpPath = webpPath;
        this.imageType = imageType;
    }

    public void updateImageType(String type) {
        this.imageType = ImageType.from(type);
    }
}
