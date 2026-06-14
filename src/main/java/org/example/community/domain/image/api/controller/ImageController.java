package org.example.community.domain.image.api.controller;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.example.community.domain.image.api.dto.response.PostImageResponse;
import org.example.community.domain.image.api.dto.response.ProfileImageResponse;
import org.example.community.domain.image.application.PostImageService;
import org.example.community.domain.image.application.ProfileImageService;
import org.example.community.global.response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final PostImageService postImageService;
    private final ProfileImageService profileImageService;

    @PostMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProfileImageResponse>> uploadProfileImage(
            @RequestPart("image") MultipartFile multipartFile) {
        ProfileImageResponse profileImageResponse = profileImageService.uploadFile(multipartFile);
        return ResponseEntity.created(URI.create("/uploads/" + profileImageResponse.id()))
                .body(ApiResponse.created(profileImageResponse));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PostImageResponse>> uploadFile(
            @RequestPart("image") MultipartFile multipartFile) {
        PostImageResponse postImageResponse = postImageService.uploadFile(multipartFile);
        return ResponseEntity.created(URI.create("/uploads/" + postImageResponse.id()))
                .body(ApiResponse.created(postImageResponse));
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<ApiResponse<PostImageResponse>> getImage(@PathVariable Long imageId) {
        return ResponseEntity.ok(ApiResponse.ok(postImageService.getImage(imageId)));
    }

    @PutMapping(value = "/profile-image/{imageId}")
    public ResponseEntity<ApiResponse<ProfileImageResponse>> updateProfileImage(@PathVariable Long imageId,
                                                      @RequestPart("image") MultipartFile multipartFile) {
        ProfileImageResponse profileImageResponse = profileImageService.updateImage(imageId, multipartFile);
        return ResponseEntity.created(URI.create("/uploads/" + profileImageResponse.id()))
                .body(ApiResponse.created(profileImageResponse));
    }

    @PutMapping(value = "/{imageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PostImageResponse>> updateFile(@PathVariable Long imageId,
                                                                     @RequestPart("image") MultipartFile multipartFile) {
        PostImageResponse postImageResponse = postImageService.updateImage(imageId, multipartFile);
        return ResponseEntity.created(URI.create("/uploads/" + postImageResponse.id()))
                .body(ApiResponse.created(postImageResponse));
    }
}
