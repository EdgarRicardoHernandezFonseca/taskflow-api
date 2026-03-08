package com.edgar.taskflow.auth.session;

import com.edgar.taskflow.auth.dto.ActiveSessionResponse;
import com.edgar.taskflow.entity.RefreshToken;
import org.springframework.stereotype.Component;

@Component
public class SessionMapper {

    public ActiveSessionResponse toResponse(RefreshToken token) {

        return ActiveSessionResponse.builder()
                .familyId(token.getFamilyId())
                .sessionStart(token.getSessionStart())
                .expiryDate(token.getExpiryDate())
                .current(false)
                .ipAddress(token.getIpAddress())
                .userAgent(token.getUserAgent())
                .deviceName(token.getDeviceName())
                .browser(token.getBrowser())
                .location(token.getLocation())
                .lastActivity(token.getLastActivity())
                .build();
    }
}