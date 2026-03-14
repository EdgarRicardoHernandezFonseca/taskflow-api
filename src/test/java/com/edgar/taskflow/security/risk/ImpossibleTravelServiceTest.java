package com.edgar.taskflow.security.risk;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ImpossibleTravelServiceTest {

    private final ImpossibleTravelService service =
            new ImpossibleTravelService();

    @Test
    void shouldReturnFalseWhenLocationsAreNull() {

        boolean result = service.isImpossibleTravel(
                null,
                "Bogota",
                LocalDateTime.now()
        );

        assertFalse(result);
    }

    @Test
    void shouldReturnFalseWhenLocationsAreSame() {

        boolean result = service.isImpossibleTravel(
                "Bogota",
                "Bogota",
                LocalDateTime.now().minusMinutes(10)
        );

        assertFalse(result);
    }

    @Test
    void shouldDetectImpossibleTravel() {

        boolean result = service.isImpossibleTravel(
                "Bogota",
                "Tokyo",
                LocalDateTime.now().minusMinutes(10)
        );

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenEnoughTimePassed() {

        boolean result = service.isImpossibleTravel(
                "Bogota",
                "Tokyo",
                LocalDateTime.now().minusHours(2)
        );

        assertFalse(result);
    }
}