# community ↔ 4-ayla-community-FE 통합 버그 수정 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `community`(Spring Boot 백엔드)와 `4-ayla-community-FE`(바닐라 JS 프론트엔드) 사이에서 실제로 깨져 있는 5가지 기능(댓글 수정, 댓글 작성자 식별, 회원가입 비밀번호 확인, 로그아웃 엔드포인트, 게시글 검색)을 고치고, 문서(API_SPEC.md/FEATURE_SPEC.md)를 실제 구현에 맞게 정정한다.

**Architecture:** 두 개의 독립된 git 저장소를 넘나드는 작업이다. 각 태스크는 한쪽 저장소(또는 양쪽)의 최소 단위 변경을 다루고, 해당 저장소 안에서 커밋한다. 백엔드는 서비스 계층 로직을 Mockito 단위 테스트로 검증하고, DB/HTTP가 필요한 부분(QueryDSL 조건, 컨트롤러 라우팅)과 프론트엔드 변경은 수동 검증(curl/브라우저)으로 확인한다 — 근거는 Global Constraints 참고.

**Tech Stack:** Backend: Java 21, Spring Boot 4.0.6, QueryDSL 5.1.0, JUnit5 + Mockito + AssertJ(`spring-boot-starter-test`). Frontend: Vanilla JS(ES Modules) + Express 정적 서버, 테스트 러너 없음(`package.json`에 jest 등 미설치).

## Global Constraints

- **참조 스펙 문서**: `community/docs/superpowers/specs/2026-08-09-community-fe-be-integration-fixes-design.md` — 모든 태스크의 요구사항은 이 문서를 따른다.
- **원격 저장소(GitHub)에 영향 금지**: 각 저장소에서 로컬 커밋만 수행한다. `git push`, PR 생성, 원격 브랜치 조작을 절대 하지 않는다.
- **저장소 경로**: 백엔드 = `/Users/kyulim/study/kakaotech_bootcamp/community` (현재 브랜치 `feature/21/deploy-cicd`). 프론트엔드 = `/Users/kyulim/study/kakaotech_bootcamp/4-ayla-community-FE`. 두 저장소는 완전히 독립적이므로 각 태스크의 커밋은 해당 저장소 안에서만 수행한다.
- **백엔드 테스트 전략**: 이 프로젝트에는 기존에 서비스/컨트롤러/리포지토리 계층 테스트가 전혀 없고(`CommunityApplicationTests`만 존재), H2 등 테스트 DB 의존성도 없다. 새 테스트 인프라(임베디드 DB, `@DataJpaTest`, `@SpringBootTest`)를 도입하는 것은 이번 버그 수정 범위를 벗어난다. 따라서:
  - Repository/Mock으로 대체 가능한 **서비스 계층 로직**은 Mockito 기반 순수 단위 테스트로 검증한다 (Spring 컨텍스트 없이 `new ServiceClass(mockA, mockB, ...)`로 직접 생성).
  - Bean Validation(`@NotBlank` 등)은 `jakarta.validation.Validation.buildDefaultValidatorFactory()`로 Spring 컨텍스트 없이 검증한다.
  - **실제 DB 쿼리(QueryDSL where 절)와 HTTP 라우팅**은 자동화 테스트 없이, 로컬에서 앱을 구동해 `curl`로 수동 검증한다 (MySQL이 `localhost:3306`에 떠 있어야 함 — `application-local.yml` 참고, 구동 명령: `./gradlew bootRun --args='--spring.profiles.active=local'`).
- **프론트엔드 테스트 전략**: `4-ayla-community-FE`는 테스트 러너가 없다(순수 브라우저 JS + Express 정적 서버). 이번 범위에서 테스트 프레임워크를 새로 도입하지 않는다. 모든 FE 변경은 `npm run dev`(nodemon, 포트 3000)로 로컬 구동 후 브라우저 개발자도구 Network 탭으로 수동 검증한다. 백엔드는 `local` 프로필로 포트 8080에 별도 구동되어 있어야 한다.
- **Java 코드 스타일**: 기존 코드베이스 패턴을 따른다 — Lombok `@RequiredArgsConstructor`, QueryDSL의 `.where(condA, condB)` vararg 형태(`CommentRepositoryImpl`에 이미 있는 패턴), 정적 팩토리 메서드(`of`/`from`).
- **커밋 메시지**: 각 저장소의 기존 커밋 스타일(한국어, `타입: 설명` 또는 자유 서술형)을 따른다.

---

## Task 1: [Backend] 댓글 작성자 정보를 객체로 확장 (Fix 2)

**Files:**
- Modify: `community/src/main/java/org/example/community/domain/post/comment/api/dto/response/CommentDetailResponse.java`
- Modify: `community/src/main/java/org/example/community/domain/post/comment/application/CommentService.java:74-83`
- Test: `community/src/test/java/org/example/community/domain/post/comment/application/CommentServiceTest.java` (신규)

**Interfaces:**
- Produces: `CommentDetailResponse.AuthorInfo(Long userId, String nickname, String profileImageUrl)` 중첩 레코드. `CommentDetailResponse.from(Comment comment)` 정적 팩토리 (기존 `from(Comment, String)`를 대체).
- Consumes: 없음 (기존 `Comment` 엔티티의 `getUser()`, `getId()`, `getContent()`, `getCreatedAt()`, `getUpdatedAt()`만 사용).

- [ ] **Step 1: 실패하는 테스트 작성**

`community/src/test/java/org/example/community/domain/post/comment/application/CommentServiceTest.java` 파일을 새로 만든다:

```java
package org.example.community.domain.post.comment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.example.community.domain.post.Post;
import org.example.community.domain.post.comment.Comment;
import org.example.community.domain.post.comment.api.dto.response.CommentDetailResponse;
import org.example.community.domain.post.comment.api.dto.response.CommentPageResponse;
import org.example.community.domain.post.comment.repository.CommentRepository;
import org.example.community.domain.post.postStatus.repository.PostStatusRepository;
import org.example.community.domain.post.repository.PostRepository;
import org.example.community.domain.user.User;
import org.example.community.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CommentServiceTest {

    private final CommentRepository commentRepository = mock(CommentRepository.class);
    private final PostRepository postRepository = mock(PostRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final PostStatusRepository postStatusRepository = mock(PostStatusRepository.class);

    private final CommentService commentService = new CommentService(
            commentRepository, postRepository, userRepository, postStatusRepository
    );

    @Test
    void getComments_returns_author_userId_nickname_and_profileImage() {
        User author = User.builder()
                .email("writer@example.com")
                .password("encoded")
                .nickname("홍길동")
                .profileImage("/uploads/profile/writer.jpg")
                .build();
        ReflectionTestUtils.setField(author, "id", 42L);

        Comment comment = Comment.builder()
                .content("댓글 내용")
                .user(author)
                .post(mock(Post.class))
                .build();
        ReflectionTestUtils.setField(comment, "id", 1L);

        when(commentRepository.findCommentsWithCursor(1L, "latest", null, 11))
                .thenReturn(List.of(comment));

        CommentPageResponse response = commentService.getComments(1L, "latest", null, 10);

        CommentDetailResponse.AuthorInfo actual = response.comments().get(0).author();
        assertThat(actual.userId()).isEqualTo(42L);
        assertThat(actual.nickname()).isEqualTo("홍길동");
        assertThat(actual.profileImageUrl()).isEqualTo("/uploads/profile/writer.jpg");
    }
}
```

- [ ] **Step 2: 테스트 실행 후 실패 확인**

Run: `./gradlew test --tests "org.example.community.domain.post.comment.application.CommentServiceTest"`
Expected: 컴파일 에러 또는 실패 — `CommentDetailResponse.author()`가 아직 `String`을 반환하므로 `.author().userId()` 호출이 컴파일되지 않는다 (`author()`의 반환 타입에 `userId()` 메서드가 없음).

- [ ] **Step 3: `CommentDetailResponse` 수정**

`community/src/main/java/org/example/community/domain/post/comment/api/dto/response/CommentDetailResponse.java` 전체를 다음으로 교체:

```java
package org.example.community.domain.post.comment.api.dto.response;

import java.time.LocalDateTime;
import org.example.community.domain.post.comment.Comment;

public record CommentDetailResponse(
        Long id,
        String content,
        AuthorInfo author,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public record AuthorInfo(Long userId, String nickname, String profileImageUrl) {}

    public static CommentDetailResponse from(Comment comment) {
        AuthorInfo author = comment.getUser() != null
                ? new AuthorInfo(
                        comment.getUser().getId(),
                        comment.getUser().getNickname(),
                        comment.getUser().getProfileImage())
                : new AuthorInfo(null, "탈퇴한 사용자", null);

        return new CommentDetailResponse(
                comment.getId(),
                comment.getContent(),
                author,
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

}
```

- [ ] **Step 4: `CommentService.getComments()` 호출부 수정**

`community/src/main/java/org/example/community/domain/post/comment/application/CommentService.java:74-83`의 다음 코드를:

```java
        return CommentPageResponse.of(
                result.stream()
                        .map(comment -> CommentDetailResponse.from(
                                comment,
                                // fetchJoin으로 이미 로딩된 상태 — 추가 쿼리 없음
                                comment.getUser() != null
                                        ? comment.getUser().getNickname()
                                        : "탈퇴한 사용자"
                        ))
                        .toList(),
                nextCursor,
                hasNext
        );
```

다음으로 교체한다:

```java
        return CommentPageResponse.of(
                result.stream()
                        .map(CommentDetailResponse::from)
                        .toList(),
                nextCursor,
                hasNext
        );
```

- [ ] **Step 5: 테스트 실행 후 통과 확인**

Run: `./gradlew test --tests "org.example.community.domain.post.comment.application.CommentServiceTest"`
Expected: PASS

- [ ] **Step 6: 전체 테스트 스위트 실행 (회귀 확인)**

Run: `./gradlew test`
Expected: 기존 `CommunityApplicationTests`를 포함해 전부 PASS (다른 곳에서 `CommentDetailResponse.from(Comment, String)` 옛 시그니처를 참조하는 곳이 없는지 컴파일 에러로 확인됨)

- [ ] **Step 7: 커밋**

```bash
cd /Users/kyulim/study/kakaotech_bootcamp/community
git add src/main/java/org/example/community/domain/post/comment/api/dto/response/CommentDetailResponse.java \
        src/main/java/org/example/community/domain/post/comment/application/CommentService.java \
        src/test/java/org/example/community/domain/post/comment/application/CommentServiceTest.java
git commit -m "fix: 댓글 작성자 정보에 userId 포함하도록 응답 구조 확장

FE가 댓글 작성자 식별에 userId를 참조하지만 기존 CommentDetailResponse.author는
닉네임 문자열뿐이라 본인 댓글 수정/삭제 버튼이 절대 노출되지 않던 문제 수정."
```

---

## Task 2: [Backend] 회원가입 비밀번호 확인 필수화 (Fix 3, 백엔드 절반)

**Files:**
- Modify: `community/src/main/java/org/example/community/domain/user/api/dto/request/UserCreateRequest.java:22`
- Test: `community/src/test/java/org/example/community/domain/user/api/dto/request/UserCreateRequestValidationTest.java` (신규)

**Interfaces:**
- Consumes: 없음 (Jakarta Bean Validation만 사용).
- Produces: `UserCreateRequest`의 `checkPassword` 필드가 blank일 때 `ConstraintViolation` 발생 (컨트롤러의 `@Valid`가 이를 잡아 `400 INVALID_INPUT`으로 응답 — 기존 `CustomRestControllerAdvice`의 `MethodArgumentNotValidException` 처리 로직 재사용, 변경 없음).

- [ ] **Step 1: 실패하는 테스트 작성**

`community/src/test/java/org/example/community/domain/user/api/dto/request/UserCreateRequestValidationTest.java` 파일을 새로 만든다:

```java
package org.example.community.domain.user.api.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class UserCreateRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void checkPassword_null_isRejected() {
        // checkPassword를 null로 사용한다: @Pattern은 null을 유효한 값으로 취급해 통과시키므로,
        // null 케이스는 오직 @NotBlank가 있어야만 위반(violation)이 발생한다 — 빈 문자열("")을 쓰면
        // @Pattern 자체가 이미 길이 규칙(8~20자) 위반으로 걸러내 버려서 @NotBlank 유무와 무관하게
        // 항상 위반이 발생하므로 이 테스트가 검증하려는 것(=@NotBlank 활성화 여부)을 구분하지 못한다.
        UserCreateRequest request = new UserCreateRequest(
                "user@example.com", "Password1!", null, "nickname", 1L
        );

        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("checkPassword"));
    }

    @Test
    void checkPassword_matchingPassword_isAccepted() {
        UserCreateRequest request = new UserCreateRequest(
                "user@example.com", "Password1!", "Password1!", "nickname", 1L
        );

        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}
```

- [ ] **Step 2: 테스트 실행 후 첫 번째 테스트 실패 확인**

Run: `./gradlew test --tests "org.example.community.domain.user.api.dto.request.UserCreateRequestValidationTest"`
Expected: `checkPassword_null_isRejected` FAIL — `checkPassword`가 `null`이면 `@Pattern`은 이를 유효한 값으로 취급해 통과시키고, `@NotBlank`는 현재 주석 처리되어 있어 적용되지 않으므로 어떤 위반(violation)도 발생하지 않아 `violations`가 빈 컬렉션이 된다. `checkPassword_matchingPassword_isAccepted`는 이미 PASS.

- [ ] **Step 3: `UserCreateRequest` 수정**

`community/src/main/java/org/example/community/domain/user/api/dto/request/UserCreateRequest.java:22`의 다음 줄:

```java
//        @NotBlank(message = ConstraintConstants.CHECK_PASSWORD_BLANK_MESSAGE)
```

을 주석 해제:

```java
        @NotBlank(message = ConstraintConstants.CHECK_PASSWORD_BLANK_MESSAGE)
```

- [ ] **Step 4: 테스트 실행 후 통과 확인**

Run: `./gradlew test --tests "org.example.community.domain.user.api.dto.request.UserCreateRequestValidationTest"`
Expected: PASS (두 테스트 모두)

- [ ] **Step 5: 커밋**

```bash
cd /Users/kyulim/study/kakaotech_bootcamp/community
git add src/main/java/org/example/community/domain/user/api/dto/request/UserCreateRequest.java \
        src/test/java/org/example/community/domain/user/api/dto/request/UserCreateRequestValidationTest.java
git commit -m "fix: 회원가입 checkPassword 필수 검증 활성화

@NotBlank가 주석 처리되어 있어 checkPassword가 비어 있어도 회원가입이
통과되던 문제 수정. FE가 checkPassword를 실제로 보내도록 하는 수정은
다음 커밋(Task 3)에서 함께 진행."
```

> **주의**: 이 커밋만 단독으로 배포/구동하면 FE가 아직 `checkPassword`를 보내지 않으므로(Task 3 이전) 모든 회원가입이 `400 INVALID_INPUT`으로 실패한다. Task 3을 반드시 이어서 진행한다. (원격 push는 하지 않으므로 로컬 히스토리상의 문제는 없지만, 로컬에서 두 저장소를 함께 띄워 수동 테스트할 때는 Task 3까지 끝난 뒤에 확인한다.)

---

## Task 3: [Frontend] 회원가입 요청에 checkPassword 포함 (Fix 3, 프론트엔드 절반)

**Files:**
- Modify: `4-ayla-community-FE/js/signup.js:38-50`

**Interfaces:**
- Consumes: Task 2에서 활성화된 백엔드 `UserCreateRequest.checkPassword` 필수 검증.
- Produces: 없음 (최종 사용자 플로우).

- [ ] **Step 1: 현재 동작 확인 (수동, 변경 전)**

1. 두 저장소를 로컬에서 함께 구동한다: 백엔드는 `cd community && ./gradlew bootRun --args='--spring.profiles.active=local'` (MySQL이 `localhost:3306`에 떠 있어야 함), 프론트엔드는 `cd 4-ayla-community-FE && npm run dev` (포트 3000).
2. `Task 2`가 이미 적용된 상태이므로, 브라우저에서 `http://localhost:3000/html/signup.html`로 이동해 회원가입을 시도한다.
3. 브라우저 개발자도구 Network 탭에서 `POST /users` 요청을 확인한다.
Expected: Task 2가 적용된 백엔드가 `checkPassword` 없이는 거부하므로 `400` 응답 확인 (Task 2 커밋이 실제로 유효함을 재확인).

- [ ] **Step 2: `signup.js` 수정**

`4-ayla-community-FE/js/signup.js:38-50`의 `sendSignupData` 함수 시작 부분:

```js
const sendSignupData = async () => {
    const { passwordCheck, ...props } = signupData;
    const profileImageId = localStorage.getItem('profileImageId');
    if (profileImageId) {
        props.imageId = Number(profileImageId);
    }
```

을 다음으로 교체 (`passwordCheck`를 버리지 않고 `checkPassword` 키로 다시 담는다):

```js
const sendSignupData = async () => {
    const { passwordCheck, ...rest } = signupData;
    const props = { ...rest, checkPassword: passwordCheck };
    const profileImageId = localStorage.getItem('profileImageId');
    if (profileImageId) {
        props.imageId = Number(profileImageId);
    }
```

- [ ] **Step 3: 수정 후 동작 확인 (수동)**

1. 백엔드/프론트엔드가 계속 구동 중인 상태에서 브라우저를 새로고침한다 (정적 파일이므로 서버 재시작 불필요, 브라우저 캐시만 새로고침).
2. `http://localhost:3000/html/signup.html`에서 이메일/비밀번호/닉네임을 모두 올바르게 입력하고 비밀번호 확인란에 **비밀번호와 다른 값**을 입력한 뒤 가입을 시도한다.
   Expected: 가입 실패 다이얼로그 표시 (`code === 'MISMATCH_PASSWORD'`는 현재 `sendSignupData`의 else 분기에서 별도 메시지가 없으므로 기본 메시지 "잠시 뒤 다시 시도해 주세요"가 뜨지만, 핵심은 **가입이 성공하면 안 된다**는 것). Network 탭에서 `POST /users` 응답이 `400`인지 확인.
3. 비밀번호 확인란에 **비밀번호와 동일한 값**을 입력하고 다시 가입을 시도한다.
   Expected: `201 Created` 응답, 로그인 페이지로 리다이렉트.

- [ ] **Step 4: 커밋**

```bash
cd /Users/kyulim/study/kakaotech_bootcamp/4-ayla-community-FE
git add js/signup.js
git commit -m "fix: 회원가입 요청에 checkPassword 필드 포함

기존에는 passwordCheck를 구조분해로 꺼내기만 하고 서버로 전송하지
않아 비밀번호 확인 검증이 무력화되어 있었음."
```

---

## Task 4: [Frontend] 댓글 수정 API 메서드 PATCH → PUT (Fix 1)

**Files:**
- Modify: `4-ayla-community-FE/api/commentRequest.js:14-27`

**Interfaces:**
- Consumes: 백엔드 `CommentController.updateComment` (`@PutMapping("/{commentId}")`, 변경 없음).
- Produces: 없음.

- [ ] **Step 1: 현재 동작 확인 (수동, 변경 전)**

1. 백엔드(`local` 프로필, 포트 8080)와 프론트엔드(포트 3000)를 구동한다.
2. 로그인 후 아무 게시글 상세 페이지(`/html/board.html?id=...`)에서 댓글을 하나 작성한다.
3. 방금 쓴 댓글의 "수정" 버튼을 눌러 내용을 바꾸고 저장을 누른다.
Expected: 개발자도구 Network 탭에서 `PATCH /posts/{postId}/comments/{commentId}` 요청이 `405`로 실패하는 것을 확인. 화면에는 "댓글 수정에 실패하였습니다" 다이얼로그가 뜬다.

- [ ] **Step 2: `commentRequest.js` 수정**

`4-ayla-community-FE/api/commentRequest.js:14-27`의 `updateComment` 함수:

```js
export const updateComment = (postId, commentId, commentContent) => {
    const result = requestJson(
        `${getServerUrl()}/posts/${postId}/comments/${commentId}`,
        {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify(commentContent),
        },
    );
    return result;
};
```

에서 `method: 'PATCH'`를 `method: 'PUT'`으로 변경한다.

- [ ] **Step 3: 수정 후 동작 확인 (수동)**

1. 브라우저를 새로고침하고 Step 1과 동일하게 댓글 수정을 시도한다.
Expected: Network 탭에서 `PUT /posts/{postId}/comments/{commentId}`가 `200 OK`로 성공. 화면이 `board.html?id=...`로 리로드되며 수정된 댓글 내용이 반영되어 있다.

- [ ] **Step 4: 커밋**

```bash
cd /Users/kyulim/study/kakaotech_bootcamp/4-ayla-community-FE
git add api/commentRequest.js
git commit -m "fix: 댓글 수정 요청 메서드를 PUT으로 변경

백엔드 CommentController는 @PutMapping만 지원하는데 FE가 PATCH로
요청해 항상 405가 발생하던 문제 수정."
```

---

## Task 5: [Frontend] 회원탈퇴/비밀번호 변경 후 로그아웃 엔드포인트 수정 (Fix 4)

**Files:**
- Modify: `4-ayla-community-FE/js/modifyInfo.js:184-209`
- Modify: `4-ayla-community-FE/js/modifyPassword.js:107-128`

**Interfaces:**
- Consumes: `utils/request.js`의 `requestJson` (이미 존재, import만 추가하면 됨). 백엔드 `AuthController.logout` (`DELETE /auth`, 변경 없음).
- Produces: 없음.

- [ ] **Step 1: 현재 동작 확인 (수동, 변경 전)**

1. 백엔드/프론트엔드 구동 후 로그인한다.
2. `/html/modifyPassword.html`에서 비밀번호를 변경한다.
Expected: 개발자도구 Network 탭에 `POST /auth/logout` 요청이 있고 `404`로 실패한다 (에러가 화면에 보이지 않음 — try/catch로 무시됨). 그럼에도 `location.href = '/html/login.html'`로 이동은 된다.

- [ ] **Step 2: `modifyPassword.js` 수정**

`4-ayla-community-FE/js/modifyPassword.js` 최상단 import 목록:

```js
import { changePassword } from '../api/modifyPasswordRequest.js';
import Dialog from '../component/dialog/dialog.js';
import Header from '../component/header/header.js';
import {
    authCheck,
    getServerUrl,
    prependChild,
    resolveImageUrl,
    validPassword,
} from '../utils/function.js';
```

에 `requestJson` import를 추가:

```js
import { changePassword } from '../api/modifyPasswordRequest.js';
import Dialog from '../component/dialog/dialog.js';
import Header from '../component/header/header.js';
import {
    authCheck,
    getServerUrl,
    prependChild,
    resolveImageUrl,
    validPassword,
} from '../utils/function.js';
import { requestJson } from '../utils/request.js';
```

그리고 `modifyPassword.js:107-128`의 `modifyPassword` 함수 안 로그아웃 호출 부분:

```js
    if (status == 200) {
        try {
            await fetch(`${getServerUrl()}/auth/logout`, {
                method: 'POST',
                credentials: 'include',
            });
        } catch (error) {
            console.error('로그아웃 요청 실패:', error);
        }
        localStorage.clear();
        location.href = '/html/login.html';
    } else {
```

에서 `fetch` 호출을 `requestJson`으로 교체:

```js
    if (status == 200) {
        try {
            await requestJson(`${getServerUrl()}/auth`, {
                method: 'DELETE',
                credentials: 'include',
            });
        } catch (error) {
            console.error('로그아웃 요청 실패:', error);
        }
        localStorage.clear();
        location.href = '/html/login.html';
    } else {
```

- [ ] **Step 3: `modifyInfo.js` 수정**

`4-ayla-community-FE/js/modifyInfo.js`는 이미 최상단에 `import { requestJson } from '../utils/request.js';`를 갖고 있으므로 import 추가는 불필요하다. `modifyInfo.js:184-209`의 `deleteAccount` 함수 안:

```js
const deleteAccount = async () => {
    const callback = async () => {
        const { status } = await userDelete(authData.data.userId);

        if (status === HTTP_OK) {
            try {
                await requestJson(`${getServerUrl()}/auth/logout`, {
                    method: 'POST',
                    credentials: 'include',
                });
            } catch (error) {
                console.error('로그아웃 요청 실패:', error);
            }
            location.href = '/html/login.html';
        } else {
            Dialog('회원 탈퇴 실패', '회원 탈퇴에 실패했습니다.');
        }
    };
```

에서 `requestJson` 호출의 URL과 메서드를 수정:

```js
const deleteAccount = async () => {
    const callback = async () => {
        const { status } = await userDelete(authData.data.userId);

        if (status === HTTP_OK) {
            try {
                await requestJson(`${getServerUrl()}/auth`, {
                    method: 'DELETE',
                    credentials: 'include',
                });
            } catch (error) {
                console.error('로그아웃 요청 실패:', error);
            }
            location.href = '/html/login.html';
        } else {
            Dialog('회원 탈퇴 실패', '회원 탈퇴에 실패했습니다.');
        }
    };
```

- [ ] **Step 4: 수정 후 동작 확인 (수동)**

1. 브라우저를 새로고침하고 로그인 후 `/html/modifyPassword.html`에서 비밀번호를 변경한다.
   Expected: Network 탭에 `DELETE /auth` 요청이 `200 OK`로 성공.
2. 다시 로그인 후 `/html/modifyInfo.html`에서 회원탈퇴를 시도한다 (테스트용 계정 사용 — 탈퇴하면 계정이 삭제됨).
   Expected: `DELETE /users` 성공 후 `DELETE /auth`도 `200 OK`로 성공.

- [ ] **Step 5: 커밋**

```bash
cd /Users/kyulim/study/kakaotech_bootcamp/4-ayla-community-FE
git add js/modifyInfo.js js/modifyPassword.js
git commit -m "fix: 회원탈퇴/비밀번호 변경 후 로그아웃 엔드포인트 수정

존재하지 않는 POST /auth/logout 대신 실제 로그아웃 엔드포인트인
DELETE /auth를 호출하도록 수정. requestJson을 사용해 Authorization
헤더가 자동으로 첨부되도록 함(header.js의 정상 동작 방식과 통일)."
```

---

## Task 6: [Backend] 게시글 검색 API 구현 (Fix 5, 백엔드 절반)

**Files:**
- Modify: `community/src/main/java/org/example/community/domain/post/api/controller/PostController.java:44-51`
- Modify: `community/src/main/java/org/example/community/domain/post/application/PostService.java:73-101`
- Modify: `community/src/main/java/org/example/community/domain/post/repository/PostRepositoryCustom.java`
- Modify: `community/src/main/java/org/example/community/domain/post/repository/PostRepositoryImpl.java`
- Test: `community/src/test/java/org/example/community/domain/post/application/PostServiceTest.java` (신규)

**Interfaces:**
- Produces: `PostRepositoryCustom.findPostsWithCursor(String sort, CursorInfo cursorInfo, int limit, String keyword)` (기존 3-인자 시그니처에 `keyword` 추가). `PostService.getPosts(String sort, String cursor, int limit, String keyword)` (기존 3-인자 시그니처에 `keyword` 추가).
- Consumes: 없음 (기존 `CursorInfo`, QueryDSL `QPost`/`QPostStatus` 재사용).

> **주의**: `findPostsWithCursor`/`getPosts`의 시그니처를 변경하므로, 이 메서드를 호출하는 다른 곳이 있는지 먼저 확인한다: `grep -rn "findPostsWithCursor\|postService.getPosts" community/src/main --include="*.java"`. 이 플랜 작성 시점 기준 호출부는 `PostController.getPosts`와 `PostService.getPosts` 내부뿐이다.

- [ ] **Step 1: 실패하는 테스트 작성**

`community/src/test/java/org/example/community/domain/post/application/PostServiceTest.java` 파일을 새로 만든다:

```java
package org.example.community.domain.post.application;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.example.community.domain.image.application.PostImageService;
import org.example.community.domain.image.repository.PostImageRepository;
import org.example.community.domain.post.comment.repository.CommentRepository;
import org.example.community.domain.post.postLike.repository.PostLikeRepository;
import org.example.community.domain.post.postStatus.ViewCountBuffer;
import org.example.community.domain.post.postStatus.repository.PostStatusRepository;
import org.example.community.domain.post.repository.PostRepository;
import org.example.community.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;

class PostServiceTest {

    private final PostRepository postRepository = mock(PostRepository.class);
    private final CommentRepository commentRepository = mock(CommentRepository.class);
    private final PostLikeRepository postLikeRepository = mock(PostLikeRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final PostStatusRepository postStatusRepository = mock(PostStatusRepository.class);
    private final ViewCountBuffer viewCountBuffer = mock(ViewCountBuffer.class);
    private final PostImageRepository postImageRepository = mock(PostImageRepository.class);
    private final PostImageService postImageService = mock(PostImageService.class);

    private final PostService postService = new PostService(
            postRepository, commentRepository, postLikeRepository, userRepository,
            postStatusRepository, viewCountBuffer, postImageRepository, postImageService
    );

    @Test
    void getPosts_passesKeywordThroughToRepository() {
        when(postRepository.findPostsWithCursor(eq("latest"), isNull(), eq(11), eq("스프링")))
                .thenReturn(List.of());

        postService.getPosts("latest", null, 10, "스프링");

        verify(postRepository).findPostsWithCursor("latest", null, 11, "스프링");
    }
}
```

- [ ] **Step 2: 테스트 실행 후 실패 확인 (컴파일 에러)**

Run: `./gradlew test --tests "org.example.community.domain.post.application.PostServiceTest"`
Expected: 컴파일 에러 — `PostService.getPosts(String, String, int, String)` 4-인자 오버로드가 아직 없음.

- [ ] **Step 3: `PostRepositoryCustom` 시그니처 변경**

`community/src/main/java/org/example/community/domain/post/repository/PostRepositoryCustom.java` 전체를:

```java
package org.example.community.domain.post.repository;

import java.util.List;
import org.example.community.domain.post.api.dto.response.PostWithStatus;
import org.example.community.global.page.CursorInfo;

public interface PostRepositoryCustom {
    List<PostWithStatus> findPostsWithCursor(String sort, CursorInfo cursorInfo, int limit, String keyword);
}
```

로 교체한다 (`keyword` 파라미터 추가).

- [ ] **Step 4: `PostRepositoryImpl`에 키워드 조건 추가**

`community/src/main/java/org/example/community/domain/post/repository/PostRepositoryImpl.java` 전체를:

```java
package org.example.community.domain.post.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.community.domain.post.QPost;
import org.example.community.domain.post.api.dto.response.PostWithStatus;
import org.example.community.domain.post.postStatus.QPostStatus;
import org.example.community.domain.user.QUser;
import org.example.community.global.page.CursorInfo;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    // cursorInfo 이전 페이지의 마지막 항목 정보. null이면 첫 페이지
    @Override
    public List<PostWithStatus> findPostsWithCursor(String sort, CursorInfo cursorInfo, int limit, String keyword) {
        QPost qPost = QPost.post;
        QPostStatus qPostStatus = QPostStatus.postStatus;
        QUser qUser = QUser.user;

        return queryFactory
                .select(Projections.constructor(PostWithStatus.class, qPost, qPostStatus))
                .from(qPost)
                .join(qPost.user, qUser).fetchJoin()
                .join(qPostStatus).on(qPostStatus.post.eq(qPost))
                .where(
                        buildCursorCondition(qPost, qPostStatus, sort, cursorInfo),
                        buildKeywordCondition(qPost, keyword)
                )
                .orderBy(buildOrderBy(qPost, qPostStatus, sort))
                .limit(limit)
                .fetch();
    }

    // 커서 조건 만들기
    private BooleanExpression buildCursorCondition(QPost qPost, QPostStatus qPostStatus,
                                                   String sort, CursorInfo cursorInfo) {
        if (cursorInfo == null) {
            return null;
        }

        return switch (sort) {
            case "latest" -> qPost.createdAt.lt(cursorInfo.getCreatedAt()) // createdAt < cursor
                    .or(qPost.createdAt.eq(cursorInfo.getCreatedAt())        // OR (createdAt = cursor
                            .and(qPost.id.lt(cursorInfo.getId())));          //     AND id < cursor.id)
            case "oldest" -> qPost.createdAt.gt(cursorInfo.getCreatedAt()) // createdAt > cursor
                    .or(qPost.createdAt.eq(cursorInfo.getCreatedAt())        // OR (createdAt = cursor
                            .and(qPost.id.gt(cursorInfo.getId())));          //     AND id > cursor.id)
            case "popular" -> qPostStatus.likeCount.lt(cursorInfo.getLikeCount()) // likeCount < cursor
                    .or(qPostStatus.likeCount.eq(cursorInfo.getLikeCount())         // OR (likeCount = cursor
                            .and(qPost.id.lt(cursorInfo.getId())));                 //     AND id < cursor.id)
            default -> null;
        };
    }

    // 키워드 조건 만들기 (제목 또는 내용에 포함, 대소문자 무시)
    private BooleanExpression buildKeywordCondition(QPost qPost, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return qPost.title.containsIgnoreCase(keyword)
                .or(qPost.content.containsIgnoreCase(keyword));
    }

    private OrderSpecifier<?>[] buildOrderBy(QPost qPost, QPostStatus qPostStatus, String sort) {
        return switch (sort) {
            case "latest" -> new OrderSpecifier[]{qPost.createdAt.desc(), qPost.id.desc()};
            case "oldest" -> new OrderSpecifier[]{qPost.createdAt.asc(), qPost.id.asc()};
            case "popular" -> new OrderSpecifier[]{qPostStatus.likeCount.desc(), qPost.id.desc()};
            default -> new OrderSpecifier[]{qPost.createdAt.desc()};
        };
    }
}
```

(변경 요지: `findPostsWithCursor`에 `String keyword` 파라미터 추가, `.where(...)`를 vararg 2개로 변경, `buildKeywordCondition` 메서드 신규 추가, `StringUtils` import 추가.)

- [ ] **Step 5: `PostService.getPosts` 수정**

`community/src/main/java/org/example/community/domain/post/application/PostService.java:73-101`의:

```java
    // 최초 요청: GET /posts?sort=latest&limit=10
    // 이후 요청: GET /posts?sort=latest&cursor=2026-05-31T12:00:00Z|1&limit=10
    // 게시글 목록 조회
    public PostPageResponse getPosts(String sort, String cursor, int limit) {

        CursorInfo cursorInfo = CursorInfo.from(cursor, sort);
        List<PostWithStatus> posts = postRepository.findPostsWithCursor(sort, cursorInfo, limit + 1);
```

를 다음으로 교체 (메서드 시그니처와 리포지토리 호출만 변경, 나머지 본문은 동일):

```java
    // 최초 요청: GET /posts?sort=latest&limit=10
    // 이후 요청: GET /posts?sort=latest&cursor=2026-05-31T12:00:00Z|1&limit=10
    // keyword가 있으면 제목/내용 검색
    // 게시글 목록 조회
    public PostPageResponse getPosts(String sort, String cursor, int limit, String keyword) {

        CursorInfo cursorInfo = CursorInfo.from(cursor, sort);
        List<PostWithStatus> posts = postRepository.findPostsWithCursor(sort, cursorInfo, limit + 1, keyword);
```

(이후 `boolean hasNext = ...`부터 메서드 끝까지는 변경 없음.)

- [ ] **Step 6: `PostController.getPosts` 수정**

`community/src/main/java/org/example/community/domain/post/api/controller/PostController.java:44-51`의:

```java
    // 게시글 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<PostPageResponse>> getPosts(
            @ModelAttribute PagingRequest pagingRequest
            ) {
        return ResponseEntity
                .ok(ApiResponse.ok(postService.getPosts(pagingRequest.sort(), pagingRequest.cursor(), pagingRequest.limit())));
    }
```

를 다음으로 교체:

```java
    // 게시글 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<PostPageResponse>> getPosts(
            @ModelAttribute PagingRequest pagingRequest,
            @RequestParam(defaultValue = "") String keyword
            ) {
        return ResponseEntity
                .ok(ApiResponse.ok(postService.getPosts(
                        pagingRequest.sort(), pagingRequest.cursor(), pagingRequest.limit(), keyword)));
    }
```

파일 상단 import 목록에 `import org.springframework.web.bind.annotation.RequestParam;`을 추가한다 (없다면).

- [ ] **Step 7: 테스트 실행 후 통과 확인**

Run: `./gradlew test --tests "org.example.community.domain.post.application.PostServiceTest"`
Expected: PASS

- [ ] **Step 8: 전체 테스트 스위트 실행 (회귀 확인)**

Run: `./gradlew test`
Expected: 전부 PASS (Task 1의 `CommentServiceTest`, Task 2의 `UserCreateRequestValidationTest` 포함)

- [ ] **Step 9: 수동으로 실제 검색 동작 확인 (curl)**

DB에 QueryDSL 테스트 인프라가 없으므로(Global Constraints 참고) 로컬 구동 후 curl로 확인한다.

1. `./gradlew bootRun --args='--spring.profiles.active=local'`로 백엔드 구동 (MySQL이 떠 있어야 함).
2. 회원가입 + 로그인으로 `accessToken`을 얻는다 (또는 이미 있는 테스트 계정 사용):
   ```bash
   curl -X POST http://localhost:8080/auth \
     -H "Content-Type: application/json" \
     -d '{"email":"test@example.com","password":"Password1!"}' | tee /tmp/login.json
   TOKEN=$(cat /tmp/login.json | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['accessToken'])")
   ```
3. 제목에 "스프링"이 들어간 게시글과 안 들어간 게시글을 각각 하나씩 만든다:
   ```bash
   curl -X POST http://localhost:8080/posts \
     -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
     -d '{"title":"스프링 부트 배웠어요","content":"내용1"}'
   curl -X POST http://localhost:8080/posts \
     -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
     -d '{"title":"오늘 점심 뭐먹지","content":"내용2"}'
   ```
4. 검색 호출:
   ```bash
   curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/posts?keyword=스프링"
   ```
   Expected: 응답의 `data.posts` 배열에 "스프링 부트 배웠어요" 게시글만 포함되고, "오늘 점심 뭐먹지"는 포함되지 않는다.
5. `keyword` 없이 호출:
   ```bash
   curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/posts"
   ```
   Expected: 두 게시글이 모두 포함된다 (기존 동작 그대로 유지되는지 회귀 확인).

- [ ] **Step 10: 커밋**

```bash
cd /Users/kyulim/study/kakaotech_bootcamp/community
git add src/main/java/org/example/community/domain/post/api/controller/PostController.java \
        src/main/java/org/example/community/domain/post/application/PostService.java \
        src/main/java/org/example/community/domain/post/repository/PostRepositoryCustom.java \
        src/main/java/org/example/community/domain/post/repository/PostRepositoryImpl.java \
        src/test/java/org/example/community/domain/post/application/PostServiceTest.java
git commit -m "feat: 게시글 목록 조회에 keyword 검색 파라미터 추가

기존 커서 기반 페이지네이션(GET /posts)에 keyword 쿼리 파라미터를
추가해 제목/내용 LIKE 검색을 지원. 별도 오프셋 기반 검색 엔드포인트를
새로 만들지 않고 기존 페이지네이션 인프라를 재사용."
```

---

## Task 7: [Frontend] 검색 기능을 통합 엔드포인트로 정리 (Fix 5, 프론트엔드 절반)

**Files:**
- Modify: `4-ayla-community-FE/api/indexRequest.js`
- Modify: `4-ayla-community-FE/js/index.js:1-40`
- Modify: `4-ayla-community-FE/html/index.html:79-82`

**Interfaces:**
- Consumes: Task 6에서 추가된 백엔드 `GET /posts?keyword=...` (기존 `sort`/`cursor`/`limit`과 함께 사용).
- Produces: 없음.

- [ ] **Step 1: 현재 동작 확인 (수동, 변경 전)**

1. 백엔드(Task 6까지 적용된 상태)와 프론트엔드를 구동한다.
2. 로그인 후 `http://localhost:3000/`에서 검색창에 아무 키워드나 입력하고 검색을 실행한다.
Expected: Network 탭에서 `GET /posts/search?keyword=...`가 `404`로 실패. 화면에는 게시글 목록이 갱신되지 않거나 에러가 콘솔에 찍힌다.

- [ ] **Step 2: `indexRequest.js` 수정**

`4-ayla-community-FE/api/indexRequest.js` 전체를:

```js
import { getServerUrl } from '../utils/function.js';
import { requestJson } from '../utils/request.js';

export const getPosts = (cursor = null, limit = 10, sort = 'latest', keyword = '') => {
    const params = new URLSearchParams({ sort, limit });
    if (cursor) params.set('cursor', cursor);
    if (keyword) params.set('keyword', keyword);
    const result = requestJson(
        `${getServerUrl()}/posts?${params.toString()}`,
        {
            credentials: 'include',
        },
    );
    return result;
};
```

로 교체한다 (`searchPosts` 함수 삭제, `getPosts`에 `keyword` 파라미터 추가).

- [ ] **Step 3: `index.js` 수정**

`4-ayla-community-FE/js/index.js:1-40`에서 import 부분:

```js
import { getPosts, searchPosts } from '../api/indexRequest.js';
```

를:

```js
import { getPosts } from '../api/indexRequest.js';
```

로 변경하고, `getBoardItem` 함수:

```js
// getBoardItem 함수
const getBoardItem = async (cursorValue = null, limitValue = 10) => {
    const result =
        currentKeyword.trim() === ''
            ? await getPosts(cursorValue, limitValue, currentSort)
            : await searchPosts(
                  currentKeyword,
                  cursorValue,
                  limitValue,
                  currentSort,
              );
    if (!result.ok) {
        throw new Error('Failed to load post list.');
    }
    return result.data;
};
```

를 다음으로 교체 (분기 제거, `getPosts` 하나로 통일):

```js
// getBoardItem 함수
const getBoardItem = async (cursorValue = null, limitValue = 10) => {
    const result = await getPosts(cursorValue, limitValue, currentSort, currentKeyword.trim());
    if (!result.ok) {
        throw new Error('Failed to load post list.');
    }
    return result.data;
};
```

- [ ] **Step 4: `index.html` 정렬 셀렉트박스 옵션 수정**

`4-ayla-community-FE/html/index.html:79-82`의:

```html
                <select id="searchSortSelect" class="sortSelect" aria-label="검색 정렬">
                    <option value="recent">최신순</option>
                    <option value="relevance">정확도순</option>
                </select>
```

를 다음으로 교체 (백엔드가 실제로 지원하는 정렬 값으로 변경):

```html
                <select id="searchSortSelect" class="sortSelect" aria-label="검색 정렬">
                    <option value="latest">최신순</option>
                    <option value="popular">인기순</option>
                </select>
```

- [ ] **Step 5: 수정 후 동작 확인 (수동)**

1. 브라우저를 새로고침하고 로그인 후 인덱스 페이지에서 특정 키워드로 검색한다.
   Expected: Network 탭에서 `GET /posts?sort=latest&limit=10&keyword=...`가 `200 OK`로 성공하고, 키워드를 포함한 게시글만 목록에 나타난다.
2. 정렬 드롭다운을 "인기순"으로 바꿔 검색 결과가 좋아요 수 기준으로 재정렬되는지 확인한다.
3. 검색창을 비우고 다시 조회했을 때 전체 게시글 목록이 정상적으로 무한 스크롤되는지 확인한다 (기존 기능 회귀 확인).

- [ ] **Step 6: 커밋**

```bash
cd /Users/kyulim/study/kakaotech_bootcamp/4-ayla-community-FE
git add api/indexRequest.js js/index.js html/index.html
git commit -m "fix: 게시글 검색을 GET /posts의 keyword 파라미터로 통합

존재하지 않는 GET /posts/search(오프셋 기반) 대신 백엔드에 새로 추가된
GET /posts?keyword= 를 사용하도록 통합. 정렬 옵션도 백엔드가 지원하는
latest/popular로 수정 (relevance는 백엔드에 구현되지 않음)."
```

---

## Task 8: [Docs] API_SPEC.md / FEATURE_SPEC.md 정정

**Files:**
- Modify: `/Users/kyulim/study/kakaotech_bootcamp/API_SPEC.md`
- Modify: `/Users/kyulim/study/kakaotech_bootcamp/FEATURE_SPEC.md`

**Interfaces:** 없음 (문서 전용, 코드 변경 없음).

> 이 두 문서는 워크스페이스 최상위(`/Users/kyulim/study/kakaotech_bootcamp`)에 있으며 이 경로는 git 저장소가 아니다. 따라서 이 태스크는 **커밋 없이 파일 수정만** 수행한다.

- [ ] **Step 1: `API_SPEC.md` — 인증 방식 정정**

`API_SPEC.md`의 "### 인증 방식" 절:

```
### 인증 방식
- Access Token: Cookie (`accessToken`)
- Refresh Token: Cookie (`refreshToken`, HttpOnly)
- 인증이 필요한 API는 `accessToken` 쿠키가 있어야 함
```

를 다음으로 교체:

```
### 인증 방식
- Access Token: `Authorization: Bearer <accessToken>` 헤더 (로그인 응답 바디의 `accessToken`을 클라이언트가 저장했다가 매 요청에 헤더로 첨부. 응답 시 `accessToken` 쿠키도 함께 내려가지만 서버는 이를 검증에 사용하지 않음)
- Refresh Token: Cookie (`refreshToken`, HttpOnly)
- 인증이 필요한 API는 `Authorization: Bearer <accessToken>` 헤더가 있어야 함
```

- [ ] **Step 2: `API_SPEC.md` — 게시글 목록 조회 인증 요구사항 및 keyword 파라미터 추가**

"### 3.2 게시글 목록 조회" 절의:

```
### 3.2 게시글 목록 조회

```
GET /posts
```

**인증 불필요**

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| sort | String | X | `latest` | 정렬 기준 (`latest`: 최신순) |
| cursor | String | X | - | 커서 기반 페이지네이션 커서값 |
| limit | Integer | X | 10 | 조회 개수 (1~50) |
```

를 다음으로 교체:

```
### 3.2 게시글 목록 조회

```
GET /posts
```

**인증 필요**

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| sort | String | X | `latest` | 정렬 기준 (`latest`: 최신순, `oldest`: 오래된순, `popular`: 인기순) |
| cursor | String | X | - | 커서 기반 페이지네이션 커서값 |
| limit | Integer | X | 10 | 조회 개수 (1~50) |
| keyword | String | X | - | 제목/내용에 포함된 키워드로 검색 (대소문자 무시) |
```

- [ ] **Step 3: `API_SPEC.md` — 댓글 목록 조회 인증 요구사항 및 author 응답 구조 정정**

"### 4.2 댓글 목록 조회" 절의:

```
### 4.2 댓글 목록 조회

```
GET /posts/{postId}/comments
```

**인증 불필요**
```

에서 `**인증 불필요**`를 `**인증 필요**`로 교체.

같은 절의 응답 예시:

```json
{
  "isSuccess": true,
  "data": {
    "comments": [
      {
        "id": 1,
        "content": "댓글 내용",
        "author": "홍길동",
        "createdAt": "2024-01-01T00:00:00",
        "updatedAt": "2024-01-01T00:00:00"
      }
    ],
    "nextCursor": "eyJ...",
    "hasNext": true
  },
  "timestamp": "2024-01-01T00:00:00"
}
```

를 다음으로 교체 (`author`를 객체로):

```json
{
  "isSuccess": true,
  "data": {
    "comments": [
      {
        "id": 1,
        "content": "댓글 내용",
        "author": {
          "userId": 1,
          "nickname": "홍길동",
          "profileImageUrl": "/uploads/profile/..."
        },
        "createdAt": "2024-01-01T00:00:00",
        "updatedAt": "2024-01-01T00:00:00"
      }
    ],
    "nextCursor": "eyJ...",
    "hasNext": true
  },
  "timestamp": "2024-01-01T00:00:00"
}
```

- [ ] **Step 4: `API_SPEC.md` — 회원가입 프로필 이미지 필드 정정**

"### 2.1 회원가입" 절의 Request Body:

```json
{
  "email": "user@example.com",
  "password": "Password1!",
  "checkPassword": "Password1!",
  "nickname": "홍길동",
  "profileImageUrl": "/uploads/profile/..."
}
```

를:

```json
{
  "email": "user@example.com",
  "password": "Password1!",
  "checkPassword": "Password1!",
  "nickname": "홍길동",
  "imageId": 1
}
```

로 교체하고, 같은 절의 필드 표:

```
| profileImageUrl | String | X | 프로필 이미지 URL |
```

를:

```
| imageId | Long | X | 업로드 API(`POST /uploads/profile-image`)로 먼저 업로드한 이미지의 ID |
```

로 교체.

- [ ] **Step 5: `API_SPEC.md` — 게시글 이미지 업로드 인증 요구사항 정정**

"### 5.2 게시글 이미지 업로드" 절의 `**인증 불필요**`를 `**인증 필요**`로 교체.

- [ ] **Step 6: `FEATURE_SPEC.md` — 인증 필터 설명에 헤더 기반 인증 명시**

"### 1.4 인증 필터 (LoginCheckFilter)" 절:

```
### 1.4 인증 필터 (LoginCheckFilter)
- 인증이 필요한 경로는 Access Token 쿠키를 검증
- 토큰이 없거나 만료/위조된 경우 `401 Unauthorized` 반환
- 인증 성공 시 요청 속성에 사용자 ID 저장 (`@LoginUser`로 주입)
```

를 다음으로 교체:

```
### 1.4 인증 필터 (LoginCheckFilter)
- 인증이 필요한 경로는 `Authorization: Bearer <accessToken>` 헤더를 검증 (쿠키는 검증에 사용하지 않음)
- 토큰이 없거나 만료/위조된 경우 `401 Unauthorized` 반환
- 인증 성공 시 요청 속성에 사용자 ID 저장 (`@LoginUser`로 주입)
```

- [ ] **Step 7: `FEATURE_SPEC.md` — 게시글/댓글 목록 조회 인증 요구사항 정정**

"### 3.2 게시글 목록 조회" 절의 `- 비로그인 사용자도 조회 가능`을 `- 로그인한 사용자만 조회 가능 (인증 필터 화이트리스트에 미포함)`으로 교체하고, 같은 절 끝에 다음 줄 추가:

```
- `keyword` 파라미터로 제목/내용 키워드 검색 가능 (LIKE 검색, 대소문자 무시)
```

"### 4.2 댓글 목록 조회" 절의 `- 비로그인 사용자도 조회 가능`을 `- 로그인한 사용자만 조회 가능`으로 교체.

- [ ] **Step 8: `FEATURE_SPEC.md` — 회원가입 프로필 이미지 설명 정정**

"### 2.1 회원가입" 절의 `- 프로필 이미지 URL 선택 입력 (업로드 API로 먼저 업로드 후 URL 전달)`을 `- 프로필 이미지 ID 선택 입력 (업로드 API로 먼저 업로드 후 반환된 imageId 전달)`으로 교체.

- [ ] **Step 9: 최종 확인**

두 문서를 처음부터 끝까지 다시 읽으며 이번에 고친 내용과 모순되는 서술이 남아있지 않은지 확인한다. 특히 "비로그인"/"쿠키"/"profileImageUrl" 문자열을 grep해서 놓친 곳이 없는지 확인:

```bash
grep -n "비로그인\|profileImageUrl" /Users/kyulim/study/kakaotech_bootcamp/API_SPEC.md /Users/kyulim/study/kakaotech_bootcamp/FEATURE_SPEC.md
```

남은 결과가 이번 수정 대상이 아닌 문맥(예: 회원 정보 조회 응답 필드로서의 `profileImageUrl`—이건 `UserInfoResponse`의 실제 필드이므로 정정 대상이 아님)이라면 그대로 둔다.

> 이 태스크는 git 저장소가 아닌 경로에서 이루어지므로 커밋 단계가 없다.

---

## 전체 작업 순서 요약

1. Task 1 (Backend) — 댓글 작성자 정보 확장
2. Task 2 (Backend) — checkPassword 필수화
3. Task 3 (Frontend) — checkPassword 전송 (Task 2 다음에 바로 실행)
4. Task 4 (Frontend) — 댓글 수정 PUT
5. Task 5 (Frontend) — 로그아웃 엔드포인트
6. Task 6 (Backend) — 검색 API
7. Task 7 (Frontend) — 검색 FE 통합 (Task 6 다음에 바로 실행)
8. Task 8 (Docs) — 문서 정정 (커밋 없음)

Task 2→3, Task 6→7 순서만 지키면 나머지는 서로 독립적이라 순서를 바꿔도 무방하다.
