package org.example.community.global.page;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PagingRequest(
        @Nullable
        String sort,
        @Nullable
        String cursor,
        @Min(1) @Max(50)
        Integer limit
) {
    public PagingRequest {
        sort = "latest";
        if (limit == null) {
            limit = 10;
        }
    }
}