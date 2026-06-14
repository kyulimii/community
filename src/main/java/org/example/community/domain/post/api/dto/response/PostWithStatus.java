package org.example.community.domain.post.api.dto.response;

import org.example.community.domain.post.Post;
import org.example.community.domain.post.postStatus.PostStatus;

public record PostWithStatus(Post post, PostStatus postStatus) {}