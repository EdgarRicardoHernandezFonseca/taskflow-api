package com.edgar.taskflow.auth;

import org.springframework.stereotype.Service;

@Service
public class UserAgentParserService {

	public String parseDeviceName(String userAgent) {

        if (userAgent == null) return "Unknown Device";

        if (userAgent.contains("Windows")) return "Windows PC";
        if (userAgent.contains("Mac")) return "Mac";
        if (userAgent.contains("Android")) return "Android Device";
        if (userAgent.contains("iPhone")) return "iPhone";
        if (userAgent.contains("iPad")) return "iPad";

        return "Unknown Device";
    }

    public String parseBrowser(String userAgent) {

        if (userAgent.contains("Chrome")) return "Chrome";
        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("Safari")) return "Safari";
        if (userAgent.contains("Edge")) return "Edge";

        return "Unknown Browser";
    }
}
