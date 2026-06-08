package org.example.community.domain.image;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image {
    // TODO: 이미지 변환(JPG/WEBP)
    @Id
    @Column(name = "image_id")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private String type;

    @Column(name = "jpg_path")
    private String jpgPath;

    @Column(name = "webp_path")
    private String webpPath;

    @Builder
    private Image(String name, String type, String jpgPath, String webpPath) {
        this.name = name;
        this.type = type;
        this.jpgPath = jpgPath;
        this.webpPath = webpPath;
    }
}
