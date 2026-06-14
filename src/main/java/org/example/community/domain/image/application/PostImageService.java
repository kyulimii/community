package org.example.community.domain.image.application;

import lombok.RequiredArgsConstructor;
import org.example.community.domain.image.PostImage;
import org.example.community.domain.image.api.dto.ConvertedImage;
import org.example.community.domain.image.api.dto.response.PostImageResponse;
import org.example.community.domain.image.repository.PostImageRepository;
import org.example.community.global.exception.CustomException;
import org.example.community.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostImageService {

    private final PostImageRepository postImageRepository;
    private final ImageValidator imageValidator;
    private final FileService fileService;
    private final ImageConverter imageConverter;

    @Transactional
    public PostImageResponse uploadFile(MultipartFile multipartFile) {
        // 1. imageValidator 호출 -> 이미지 검증
        imageValidator.validate(multipartFile);

        // 2. jpg, webp로 변환
        ConvertedImage convertedImage = imageConverter.converter(multipartFile);

        // 3. Image 저장, 임시 파일 꼭 삭제
        String originalName = multipartFile.getOriginalFilename();

        try {
            String jpgPath = fileService.uploadFile(convertedImage.jpgFile(), originalName + ".jpg");
            String webpPath = fileService.uploadFile(convertedImage.webpFile(), originalName + ".webp");

            PostImage image = PostImage.builder()
                    .originalName(originalName)
                    .jpgPath(jpgPath)
                    .webpPath(webpPath)
                    .build();

            postImageRepository.save(image);
            return PostImageResponse.from(image);
        } finally {
            convertedImage.jpgFile().delete();
            convertedImage.webpFile().delete();
        }
    }

    // 이미지 조회
    public PostImageResponse getImage(Long imageId) {
        PostImage image = postImageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_IMAGE));
        return PostImageResponse.from(image);
    }

    @Transactional
    public PostImageResponse updateImage(Long imageId, MultipartFile multipartFile) {
        PostImage image = postImageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_IMAGE));
        deleteImage(image.getJpgPath());

        return uploadFile(multipartFile);
    }

    @Transactional
    public void deleteImage(String imageUrl) {
        // 1. DB에서 기존 이미지 경로 조회
        PostImage image = postImageRepository.findByJpgPath(imageUrl)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_IMAGE));

        // 2. 로컬 파일 삭제 (경로를 알아야 지울 수 있음)
        fileService.deleteFile(image.getJpgPath());
        fileService.deleteFile(image.getWebpPath());

        // 3. DB 레코드 삭제
        postImageRepository.delete(image);
    }
}
