package org.example.community.domain.image.application;

import lombok.RequiredArgsConstructor;
import org.example.community.domain.image.Image;
import org.example.community.domain.image.api.dto.ConvertedImage;
import org.example.community.domain.image.api.dto.response.ImageResponse;
import org.example.community.domain.image.repository.ImageRepository;
import org.example.community.global.exception.CustomException;
import org.example.community.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final ImageValidator imageValidator;
    private final FileService fileService;
    private final ImageConverter imageConverter;

    public ImageResponse uploadFile(MultipartFile profileImage) {
        // 1. imageValidator 호출 -> 이미지 검증
        imageValidator.validate(profileImage);

        // 2. jpg, webp로 변환
        ConvertedImage convertedImage = imageConverter.converter(profileImage);

        // 3. Image 저장, 임시 파일 꼭 삭제
        String originalName = profileImage.getOriginalFilename();

        try {
            String jpgPath = fileService.uploadFile(convertedImage.jpgFile(), originalName + ".jpg");
            String webpPath = fileService.uploadFile(convertedImage.webpFile(), originalName + ".webp");

            Image image = Image.builder()
                    .originalName(originalName)
                    .jpgPath(jpgPath)
                    .webpPath(webpPath)
                    .imageType(null)
                    .build();

            imageRepository.save(image);
            return ImageResponse.from(image);
        } finally {
            convertedImage.jpgFile().delete();
            convertedImage.webpFile().delete();
        }
    }

    // 이미지 조회
    public ImageResponse getImage(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_IMAGE));
        return ImageResponse.from(image);
    }
}
