# =========================================================
# 1) build  단계: 소스를 컴파일해서 실행 가능한 fat jar를 생성
#    - JDK가 필요하므로 이 단계에서만 jdk 이미지를 사용
#    - alpine 계열 이미지를 사용해 기본 OS 용량을 최소화
# =========================================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Gradle wrapper 및 빌드 설정 파일만 먼저 복사 → 소스 변경과 무관하게 의존성 레이어 캐싱
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# gradlew를 실행하기 전에 실행 권한 부여 후, 의존성만 미리 받아 별도 레이어로 캐싱
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon

# 소스는 마지막에 복사 (소스만 바뀌었을 때 위 의존성 레이어는 캐시 재사용)
COPY src ./src

# test/check 등 부가 태스크 없이 실행 가능한 jar만 생성 → 빌드 단계 자체를 가볍게
RUN ./gradlew bootJar --no-daemon -x test

# Spring Boot의 jarmode=tools 기능으로 fat jar를 레이어별로 분해
#   dependencies         : 외부 라이브러리 (커밋마다 거의 안 바뀜 → 캐시 재사용률 최고)
#   spring-boot-loader   : 부트 런처 클래스 (거의 안 바뀜)
#   snapshot-dependencies: 스냅샷 버전 의존성 (가끔 바뀜)
#   application          : 우리가 작성한 클래스/리소스 (매 배포마다 바뀜)
# 이렇게 나누면 최종 이미지에서도 각 레이어가 그대로 유지되어,
# 코드만 수정했을 때 registry에 새로 push/pull 되는 용량이 fat jar 전체(수십MB)가
# 아니라 application 레이어(보통 수백KB~수MB)로 줄어듦
RUN java -Djarmode=tools -jar build/libs/*-SNAPSHOT.jar extract --layers --launcher --destination extracted

# =========================================================
# 2) runtime 단계: 실행에만 필요한 최소 구성으로 이미지 생성
#    - JDK 대신 JRE alpine 이미지 사용 (컴파일러 등 불필요한 도구 제외 → 용량 절감)
#    - non-root 사용자로 실행해 컨테이너 탈취 시 피해 범위 최소화
# =========================================================
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -g 1000 worker && \
    adduser -u 1000 -G worker -s /bin/sh -D worker

# WORKDIR로 생성된 /app은 root 소유(755)라 worker가 새 파일/디렉토리를 만들 수 없음
# → 업로드 파일이 실제로 쓰이는 uploads 디렉토리만 미리 만들어 worker에게 소유권을 넘김
# (/app 전체를 worker 소유로 바꾸지 않고 필요한 디렉토리만 열어 최소 권한 유지)
RUN mkdir -p /app/uploads && chown worker:worker /app/uploads

# 변경 빈도가 낮은 레이어부터 순서대로 복사 → Docker 레이어 캐시 적중률 극대화
COPY --from=builder --chown=worker:worker /app/extracted/dependencies/ ./
COPY --from=builder --chown=worker:worker /app/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=worker:worker /app/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=worker:worker /app/extracted/application/ ./

USER worker:worker

EXPOSE 8080

# fat jar를 통째로 실행(java -jar)하지 않고 분해된 클래스를 JarLauncher로 직접 구동
# → 압축 해제 과정이 없어 시작 속도가 빠르고, 위에서 나눈 레이어 구조를 그대로 활용
# -XX:MaxRAMPercentage: 컨테이너에 할당된 메모리 한도를 기준으로 힙 크기를 잡도록 하여
#                        컨테이너 환경에서 JVM이 과도하게(또는 너무 적게) 메모리를 잡는 것을 방지
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "org.springframework.boot.loader.launch.JarLauncher"]

# docker-compose.yml 도입 시 프로필 제거
CMD ["--spring.profiles.active=dev"]
