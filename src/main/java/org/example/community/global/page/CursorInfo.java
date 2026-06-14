package org.example.community.global.page;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import lombok.Getter;
import org.example.community.global.exception.CustomException;
import org.example.community.global.exception.ErrorCode;

@Getter
public class CursorInfo {
    private final LocalDateTime createdAt;  // latest, oldest용
    private final Integer likeCount;           // popular용
    private final Long id;

    private CursorInfo(LocalDateTime createdAt, Integer likeCount, Long id) {
        this.createdAt = createdAt;
        this.likeCount = likeCount;
        this.id = id;
    }

    public static CursorInfo from(String cursor, String sort) {
        if (cursor == null) {
            return null;
        }

        try {
            // Base64 디코딩하여 cursorInfo 생성
            String decoded = new String(Base64.getUrlDecoder().decode(cursor));
            String[] parts = decoded.split("\\|");

            // 좋아요순
            if (sort.equals("popular")) {
                return new CursorInfo(null, Integer.parseInt(parts[0]), Long.valueOf(parts[1]));
            }

            // 최신순/오래된순
            LocalDateTime createdAt = Instant.parse(parts[0]).atZone(ZoneId.of("UTC")).toLocalDateTime();
            return new CursorInfo(createdAt, null, Long.valueOf(parts[1]));
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_CURSOR);
        }
    }

    // latest, oldest용 (댓글, 게시글 공통)
    public static String encode(LocalDateTime createdAt, Long id) {
        String raw = createdAt.atZone(ZoneId.of("UTC")).toInstant() + "|" + id;
        return Base64.getUrlEncoder().encodeToString(raw.getBytes());
    }

    // popular용 (게시글 전용)
    public static String encode(int likeCount, Long id) {
        String raw = likeCount + "|" + id;
        return Base64.getUrlEncoder().encodeToString(raw.getBytes());
    }
}