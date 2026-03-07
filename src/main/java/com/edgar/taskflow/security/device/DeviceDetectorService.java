package com.edgar.taskflow.security.device;

import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.stereotype.Service;

import com.edgar.taskflow.auth.dto.DeviceInfo;

@Service
public class DeviceDetectorService {

    public DeviceInfo detect(String userAgentString) {

        if (userAgentString == null) {
            return new DeviceInfo("Unknown Device", "Unknown", "Unknown");
        }

        UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);

        String browser = userAgent.getBrowser().getName();
        String os = userAgent.getOperatingSystem().getName();

        String device;

        if (userAgent.getOperatingSystem().isMobileDevice()) {
            device = "Mobile";
        } else {
            device = "Desktop";
        }

        return new DeviceInfo(device, browser, os);
    }
}
