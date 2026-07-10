package org.example.community.domain.user.application;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.example.community.domain.auth.repository.RefreshTokenRepository;
import org.example.community.domain.image.ProfileImage;
import org.example.community.domain.image.application.ProfileImageService;
import org.example.community.domain.image.repository.ProfileImageRepository;
import org.example.community.domain.post.comment.repository.CommentRepository;
import org.example.community.domain.post.postLike.repository.PostLikeRepository;
import org.example.community.domain.post.repository.PostRepository;
import org.example.community.domain.user.User;
import org.example.community.domain.user.api.dto.request.UserCreateRequest;
import org.example.community.domain.user.api.dto.request.UserPasswordUpdateRequest;
import org.example.community.domain.user.api.dto.request.UserUpdateRequest;
import org.example.community.domain.user.api.dto.response.UserInfoResponse;
import org.example.community.domain.user.repository.UserRepository;
import org.example.community.global.security.PasswordEncoder;
import org.example.community.global.exception.CustomException;
import org.example.community.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ProfileImageRepository profileImageRepository;
    private final ProfileImageService profileImageService;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Transactional
    public Long signup(UserCreateRequest userCreateRequest) {
        validateEmailDuplication(userCreateRequest.email());
        validateNicknameDuplication(userCreateRequest.nickname());
        validatePassword(userCreateRequest.password(), userCreateRequest.checkPassword());

        ProfileImage profileImage = profileImageRepository.findById(userCreateRequest.imageId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_IMAGE));

        User user = User.builder()
                .email(userCreateRequest.email())
                .password(passwordEncoder.encode(userCreateRequest.password()))
                .nickname(userCreateRequest.nickname())
                .profileImage(profileImage.getJpgPath())
                .build();

        userRepository.save(user);
        return user.getId();
    }

    // 회원탈퇴
    @Transactional
    public void deleteUser(Long loginUserId) {
        User user = findUserById(loginUserId);

        postRepository.deleteByUserId(loginUserId);
        commentRepository.deleteByUserId(loginUserId);
        postLikeRepository.deleteByUserId(loginUserId);
        refreshTokenRepository.deleteByUserId(loginUserId);
        profileImageService.deleteImage(user.getProfileImage());

        userRepository.deleteById(loginUserId);
    }

    // 이메일 중복 검사
    public void validateEmailDuplication(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.DUPLICATION_EMAIL);
        }
    }

    // 닉네임 중복 검사
    public void validateNicknameDuplication(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.DUPLICATION_NICKNAME);
        }
    }

    // 내 정보 조회 (/users/me)
    public UserInfoResponse getMyInfo(Long loginUserId) {
        return UserInfoResponse.from(findUserById(loginUserId));
    }

    // 회원 정보 조회
    public UserInfoResponse getUserInfo(Long userId, Long loginUserId) {
        validateLogin(userId, loginUserId);
        return UserInfoResponse.from(findUserById(userId));
    }

    // 회원 정보 수정 - 닉네임, 프로필 사진
    @Transactional
    public void updateUserInfo(Long userId, Long loginUserId, UserUpdateRequest userUpdateRequest) {
        validateLogin(userId, loginUserId);

        User user = findUserById(userId);
        String nickname = userUpdateRequest.nickname();

        if (!nickname.equals(user.getNickname())) {
            validateNicknameDuplication(nickname);
        }

        String newImageUrl = userUpdateRequest.profileImageUrl();
        String currentImageUrl = user.getProfileImage();

        if (newImageUrl != null && !newImageUrl.equals(currentImageUrl)) {
            if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                profileImageService.deleteImage(currentImageUrl);
            }
            ProfileImage newImage = profileImageRepository.findByJpgPath(newImageUrl)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_IMAGE));
            newImage.assignToUser(user);
            user.updateUserInfo(nickname, newImageUrl);
        } else {
            user.updateUserInfo(nickname, currentImageUrl);
        }

        userRepository.save(user);
    }

    // 회원 비밀번호 수정
    @Transactional
    public void updateUserPassword(Long userId, Long loginUserId,
                                   UserPasswordUpdateRequest userPasswordUpdateRequest) {
        validateLogin(userId, loginUserId);
        User user = findUserById(userId);
        matchPassword(user, userPasswordUpdateRequest.currentPassword());
        validatePassword(userPasswordUpdateRequest.password(), userPasswordUpdateRequest.checkPassword());
        user.updatePassword(passwordEncoder.encode(userPasswordUpdateRequest.password()));
        userRepository.save(user);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
    }

    private void validatePassword(String password, String checkPassword) {
        if (checkPassword != null && !password.equals(checkPassword)) {
            throw new CustomException(ErrorCode.MISMATCH_PASSWORD);
        }
    }

    private void validateLogin(Long userId, Long loginUserId) {
        if (!Objects.equals(loginUserId, userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    private void matchPassword(User user, String currentPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new CustomException(ErrorCode.MISMATCH_PASSWORD);
        }
    }
}
