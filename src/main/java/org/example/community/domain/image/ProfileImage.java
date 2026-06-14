package org.example.community.domain.image;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.community.domain.user.User;
import org.example.community.global.base.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileImage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_image_id", nullable = false)
    private Long id;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "jpg_path", nullable = false)
    private String jpgPath;

    @Column(name = "webp_path", nullable = false)
    private String webpPath;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Builder
    private ProfileImage(String originalName, String jpgPath, String webpPath) {
        this.originalName = originalName;
        this.jpgPath = jpgPath;
        this.webpPath = webpPath;
    }

    public void assignToUser(User user) {
        this.user = user;
    }
}
