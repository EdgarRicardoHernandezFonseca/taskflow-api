package com.edgar.taskflow.security.device;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeviceDetectorServiceTest {

    private final DeviceDetectorService service =
            new DeviceDetectorService();

    @Test
    void shouldDetectDesktopDevice() {

        String ua =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0";

        DeviceInfo device = service.detect(ua);

        assertEquals("Desktop", device.getDevice());
        assertNotNull(device.getBrowser());
        assertNotNull(device.getOs());
    }

    @Test
    void shouldReturnUnknownWhenUserAgentNull() {

        DeviceInfo device = service.detect(null);

        assertEquals("Unknown Device", device.getDevice());
        assertEquals("Unknown", device.getBrowser());
        assertEquals("Unknown", device.getOs());
    }
}