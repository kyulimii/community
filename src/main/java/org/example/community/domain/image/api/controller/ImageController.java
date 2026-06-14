package org.example.community.domain.image.api.controller;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.example.community.domain.image.api.dto.response.ImageResponse;
import org.example.community.domain.image.application.ImageService;
import org.example.community.global.response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/uploads")
public class ImageController {
    private final ImageService imageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageResponse>> uploadFile(
            @RequestPart("image") MultipartFile multipartFile) {
        ImageResponse imageResponse = imageService.uploadFile(multipartFile);
        return ResponseEntity.created(URI.create("/uploads/" + imageResponse.id()))
                .body(ApiResponse.created(imageResponse));
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<ApiResponse<ImageResponse>> getImage(@PathVariable Long imageId) {
        return ResponseEntity.ok(ApiResponse.ok(imageService.getImage(imageId)));
    }

    @PutMapping(value = "/{imageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageResponse>> updateFile(@PathVariable Long imageId,
                                                                 @RequestPart("image") MultipartFile multipartFile) {
        ImageResponse imageResponse = imageService.updateImage(imageId, multipartFile);
        return ResponseEntity.created(URI.create("/uploads/" + imageResponse.id()))
                .body(ApiResponse.created(imageResponse));
    }
}
