package com.edgar.taskflow.auth.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActiveSessionResponse {

	private String familyId;
    private LocalDateTime sessionStart;
    private LocalDateTime expiryDate;
    private boolean current;
    private String ipAddress;
    private String userAgent;
}
