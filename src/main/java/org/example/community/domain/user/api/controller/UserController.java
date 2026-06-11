package org.example.community.domain.user.api.controller;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.example.community.domain.user.api.dto.request.UserCreateRequest;
import org.example.community.domain.user.api.dto.request.UserPasswordUpdateRequest;
import org.example.community.domain.user.api.dto.request.UserUpdateRequest;
import org.example.community.domain.user.api.dto.response.UserInfoResponse;
import org.example.community.domain.user.application.UserService;
import org.example.community.global.resolver.LoginUser;
import org.example.community.global.response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> signup(
            @RequestPart("userInfo") @Valid UserCreateRequest userCreateRequest,
            @RequestPart("profileImage") MultipartFile profileImage) {
        Long id = userService.signup(userCreateRequest, profileImage);
        return ResponseEntity
                .created(URI.create("/users/" + id))
                .body(ApiResponse.created(null));
    }

    // 내 정보 조회 - 인증 상태 확인 + 유저 정보 반환
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getMyInfo(@LoginUser Long loginUserId) {
        return ResponseEntity
                .ok(ApiResponse.ok(userService.getMyInfo(loginUserId)));
    }

    // 회원탈퇴
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteUser(@LoginUser Long loginUserId) {
        userService.deleteUser(loginUserId);
        return ResponseEntity
                .ok(ApiResponse.ok(null));
    }

    // 이메일 중복 검사
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<Void>> validateEmailDuplication(@PathVariable String email) {
        userService.validateEmailDuplication(email);
        return ResponseEntity
                .ok(ApiResponse.ok(null));
    }

    // 닉네임 중복 검사
    @GetMapping("/nickname/{nickname}")
    public ResponseEntity<ApiResponse<Void>> validateNicknameDuplication(@PathVariable String nickname) {
        userService.validateNicknameDuplication(nickname);
        return ResponseEntity
                .ok(ApiResponse.ok(null));
    }

    // 회원 정보 조회
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserInfo(@PathVariable Long userId,
                                                                     @LoginUser Long loginUserId) {
        return ResponseEntity
                .ok(ApiResponse.ok(userService.getUserInfo(userId, loginUserId)));
    }

    // 회원 정보 수정 - 닉네임, 프로필 사진
    @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> updateUserInfo(@PathVariable Long userId,
                                                            @LoginUser Long loginUserId,
                                                            @RequestPart(value = "nickname", required = false) @Valid UserUpdateRequest userUpdateRequest,
                                                            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        userService.updateUserInfo(userId, loginUserId, userUpdateRequest, profileImage);
        return ResponseEntity
                .ok(ApiResponse.ok(null));
    }

    // 회원 비밀번호 수정
    @PutMapping("/{userId}/password")
    public ResponseEntity<ApiResponse<Void>> updateUserPassword(@PathVariable Long userId,
                                                                @LoginUser Long loginUserId,
                                                                @RequestBody @Valid UserPasswordUpdateRequest userPasswordUpdateRequest) {
        userService.updateUserPassword(userId, loginUserId, userPasswordUpdateRequest);
        return ResponseEntity
                .ok(ApiResponse.ok(null));
    }
}
