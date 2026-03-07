package com.edgar.taskflow.security.device;

import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.stereotype.Service;

@Service
public class DeviceDetectorService {

    public String detectDevice(String userAgentString) {

        if (userAgentString == null) {
            return "Unknown Device";
        }

        UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);

        String browser = userAgent.getBrowser().getName();
        String os = userAgent.getOperatingSystem().getName();

        return os + " - " + browser;
    }
}
