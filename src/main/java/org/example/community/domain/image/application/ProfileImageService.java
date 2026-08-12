package org.example.community.domain.image.application;

import lombok.RequiredArgsConstructor;
import org.example.community.domain.image.ProfileImage;
import org.example.community.domain.image.api.dto.ConvertedImage;
import org.example.community.domain.image.api.dto.response.ProfileImageResponse;
import org.example.community.domain.image.repository.ProfileImageRepository;
import org.example.community.global.exception.CustomException;
import org.example.community.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileImageService {

    private final ProfileImageRepository profileImageRepository;
    private final ImageValidator imageValidator;
    private final FileService fileService;
    private final ImageConverter imageConverter;

    @Transactional
    public ProfileImageResponse uploadFile(MultipartFile multipartFile) {
        // 1. imageValidator 호출 -> 이미지 검증
        imageValidator.validate(multipartFile);

        // 2. jpg, webp로 변환
        ConvertedImage convertedImage = imageConverter.converter(multipartFile);

        // 3. Image 저장, 임시 파일 꼭 삭제
        String originalName = multipartFile.getOriginalFilename();
        String baseName = (originalName != null && originalName.contains("."))
                ? originalName.substring(0, originalName.lastIndexOf('.'))
                : originalName;

        try {
            String jpgPath = fileService.uploadFile(convertedImage.jpgFile(), baseName + ".jpg");
            String webpPath = fileService.uploadFile(convertedImage.webpFile(), baseName + ".webp");

            ProfileImage image = ProfileImage.builder()
                    .originalName(originalName)
                    .jpgPath(jpgPath)
                    .webpPath(webpPath)
                    .build();

            profileImageRepository.save(image);
            return ProfileImageResponse.from(image);
        } finally {
            convertedImage.jpgFile().delete();
            convertedImage.webpFile().delete();
        }
    }

    // 이미지 조회
    public ProfileImageResponse getImage(Long imageId) {
        ProfileImage image = profileImageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_IMAGE));
        return ProfileImageResponse.from(image);
    }

    @Transactional
    public ProfileImageResponse updateImage(Long imageId, MultipartFile multipartFile) {
        ProfileImage image = profileImageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_IMAGE));
        deleteImage(image.getJpgPath());

        return uploadFile(multipartFile);
    }

    @Transactional
    public void deleteImage(String imageUrl) {
        if (imageUrl == null) {
            return;
        }

        // 1. DB에서 기존 이미지 경로 조회
        ProfileImage image = profileImageRepository.findByJpgPath(imageUrl)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_IMAGE));

        // 2. 로컬 파일 삭제 (경로를 알아야 지울 수 있음)
        fileService.deleteFile(image.getJpgPath());
        fileService.deleteFile(image.getWebpPath());

        // 3. DB 레코드 삭제
        profileImageRepository.delete(image);
    }
}
