package org.example.community.domain.post.postLike;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode // Serializable
@NoArgsConstructor
public class PostLikeId implements Serializable {

    private Long userId;
    private Long postId;
}