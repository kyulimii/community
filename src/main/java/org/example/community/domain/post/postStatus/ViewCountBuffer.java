package org.example.community.domain.post.postStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class ViewCountBuffer {

    // key: postId, value: 누적 조회수 증가량
    private final ConcurrentHashMap<Long, AtomicInteger> buffer = new ConcurrentHashMap<>();

    public void increment(Long postId) {
        buffer.computeIfAbsent(postId, id -> new AtomicInteger())
                .incrementAndGet();
    }

    public Map<Long, Integer> getAndReset() {
        Map<Long, Integer> snapshot = new HashMap<>();

        buffer.forEach((postId, counter) -> {
            int count = counter.getAndSet(0);

            if (count > 0) {
                snapshot.put(postId, count);
            }
        });

        return snapshot;
    }
}