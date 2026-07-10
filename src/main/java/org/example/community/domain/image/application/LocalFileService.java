package org.example.community.domain.image.application;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.community.global.exception.CustomException;
import org.example.community.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalFileService implements FileService {

    @Value("${file.upload.dir}")
    private String uploadDir;

    @Override
    public String uploadFile(File file, String originalFilename) {
        Path directory = Paths.get(uploadDir).toAbsolutePath().normalize();
        String filename = UUID.randomUUID() + "_" + originalFilename;
        Path targetPath = directory.resolve(filename);

        try {
            Files.createDirectories(directory);
            Files.copy(file.toPath(), targetPath);

            log.info("이미지 저장 성공 - source: {}, target: {}", file.toPath(), targetPath);
            return "/uploads/" + filename;
        } catch (IOException e) {
            log.error("이미지 저장 실패 - directory: {}, target: {}", directory, targetPath, e);
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        Path fsPath = Paths.get(uploadDir).toAbsolutePath().normalize()
                .resolve(filePath.startsWith("/uploads/") ? filePath.substring("/uploads/".length()) : filePath);

        try {
            if (!Files.deleteIfExists(fsPath)) {
                log.warn("삭제할 이미지 파일을 찾을 수 없음 - path: {}", fsPath);
                throw new CustomException(ErrorCode.NOT_FOUND_IMAGE);
            }
        } catch (IOException e) {
            log.error("이미지 삭제 실패 - path: {}", fsPath, e);
            throw new CustomException(ErrorCode.FILE_DELETE_FAILED);
        }
    }
}
