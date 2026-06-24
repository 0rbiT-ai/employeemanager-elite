package com.elite.employeemanager.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamsMessageRequest {
    private Body body;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Body {
        private String content;
    }
}
