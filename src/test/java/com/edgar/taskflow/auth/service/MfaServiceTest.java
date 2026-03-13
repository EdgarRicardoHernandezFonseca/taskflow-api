package com.edgar.taskflow.auth.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MfaServiceTest {

    private final MfaService mfaService = new MfaService();

    @Test
    void shouldReturnTrueWhenRiskDetected() {

        boolean result = mfaService.isStepUpRequired(true);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenRiskNotDetected() {

        boolean result = mfaService.isStepUpRequired(false);

        assertFalse(result);
    }
}
