package com.edgar.taskflow.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import org.springframework.stereotype.Service;

@Service
public class DeviceFingerprintService {
	
	public String generateFingerprint(String ip, String userAgent) {

        try {

            String raw = ip + ":" + userAgent;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new RuntimeException("Fingerprint generation failed");
        }
    }

}
