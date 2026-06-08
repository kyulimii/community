package org.example.community.domain.image.application;

import org.springframework.web.multipart.MultipartFile;

// 클라우드 확장 고려한 다형성 구현
public interface FileService {
    String uploadFile(MultipartFile file);
}
