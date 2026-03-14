package com.edgar.taskflow.security;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

class DeviceFingerprintServiceTest {

    private final DeviceFingerprintService service =
            new DeviceFingerprintService();

    @Test
    void shouldGenerateFingerprint() {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader("User-Agent", "Chrome");
        request.setRemoteAddr("127.0.0.1");

        String fingerprint =
                service.generateFingerprint(request);

        assertNotNull(fingerprint);
        assertFalse(fingerprint.isEmpty());
    }

    @Test
    void shouldGenerateSameFingerprintForSameInput() {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader("User-Agent", "Chrome");
        request.setRemoteAddr("127.0.0.1");

        String fp1 = service.generateFingerprint(request);
        String fp2 = service.generateFingerprint(request);

        assertEquals(fp1, fp2);
    }
}