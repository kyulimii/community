package org.example.community.domain.post.postStatus;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.community.domain.post.postStatus.repository.PostStatusRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountScheduler {

    private final ViewCountBuffer viewCountBuffer;
    private final PostStatusRepository postStatusRepository;

    // 30초마다 실행 (단위: ms)
    // fixedDelay: 이전 실행 완료 후 30초 대기 → 실행 시간이 길어져도 중첩 실행 없음
    // fixedRate와의 차이: fixedRate는 시작 기준 30초, 실행이 오래 걸리면 중첩 가능
    @Scheduled(fixedDelay = 30000)
    public void flushViewCount() {
        Map<Long, Integer> snapshot = viewCountBuffer.getAndReset();

        if (snapshot.isEmpty()) {
            return;
        }

        snapshot.forEach((postId, count) -> {
            try {
                postStatusRepository.incrementViewCount(postId, count);
            } catch (Exception e) {
                // 특정 postId 실패 시 나머지는 계속 처리
                // 유실은 허용 (조회수 특성상 정확도보다 가용성 우선)
                log.warn("viewCount flush 실패 - postId: {}, count: {}", postId, count);
            }
        });
    }
}