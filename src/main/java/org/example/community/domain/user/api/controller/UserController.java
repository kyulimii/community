package org.example.community.domain.user.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.community.domain.user.api.dto.request.UserCreateRequestDto;
import org.example.community.domain.user.api.dto.request.UserPasswordUpdateRequestDto;
import org.example.community.domain.user.api.dto.response.UserInfoDto;
import org.example.community.domain.user.api.dto.response.UserUpdateRequestDto;
import org.example.community.domain.user.application.UserService;
import org.example.community.global.resolver.LoginUser;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<Void> signup(
            @RequestPart("userInfo") @Valid UserCreateRequestDto userCreateRequestDto,
            @RequestPart("profileImage") MultipartFile profileImage) {
        userService.signup(userCreateRequestDto, profileImage);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 회원탈퇴
    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@LoginUser Long loginUserId) {
        userService.deleteUser(loginUserId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 이메일 중복 검사
    @GetMapping("/email/{email}")
    public ResponseEntity<Void> validateEmailDuplication(@PathVariable String email) {
        userService.validateEmailDuplication(email);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 닉네임 중복 검사
    @GetMapping("/nickname/{nickname}")
    public ResponseEntity<Void> validateNicknameDuplication(@PathVariable String nickname) {
        userService.validateNicknameDuplication(nickname);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 회원 정보 조회
    @GetMapping("/{userId}")
    public ResponseEntity<UserInfoDto> getUserInfo(@PathVariable Long userId,
                                                   @LoginUser Long loginUserId) {
        return ResponseEntity.ok(userService.getUserInfo(userId, loginUserId));
    }

    // 회원 정보 수정 - 닉네임, 프로필 사진
    @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateUserInfo(@PathVariable Long userId,
                                               @LoginUser Long loginUserId,
                                               @RequestPart(value = "nickname", required = false) @Valid UserUpdateRequestDto userUpdateRequestDto,
                                               @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        userService.updateUserInfo(userId, loginUserId, userUpdateRequestDto, profileImage);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 회원 비밀번호 수정
    @PutMapping("/{userId}")
    public ResponseEntity<Void> updateUserPassword(@PathVariable Long userId,
                                                   @LoginUser Long loginUserId,
                                                   @RequestBody @Valid UserPasswordUpdateRequestDto userPasswordUpdateRequestDto) {
        userService.updateUserPassword(userId, loginUserId, userPasswordUpdateRequestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
