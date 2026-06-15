package org.example.community.domain.image.application;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.community.domain.image.repository.PostImageRepository;
import org.example.community.global.exception.CustomException;
import org.example.community.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocalFileService implements FileService {

    private final PostImageRepository postImageRepository;

    @Value("${file.upload.dir}")
    private String uploadDir;

    @Override
    public String uploadFile(File file, String originalFilename) {
        try {
            Path directory = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(directory);

            String filename = UUID.randomUUID() + "_" + originalFilename;
            Path targetPath = directory.resolve(filename);

            Files.copy(file.toPath(), targetPath);

            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        Path fsPath = Paths.get(filePath.startsWith("/") ? filePath.substring(1) : filePath);

        try {
            if (!Files.deleteIfExists(fsPath)) {
                throw new CustomException(ErrorCode.NOT_FOUND_IMAGE);
            }
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_DELETE_FAILED);
        }
    }
}
