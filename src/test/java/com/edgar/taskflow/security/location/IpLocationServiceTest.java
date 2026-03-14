package com.edgar.taskflow.security.location;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IpLocationServiceTest {

    private final IpLocationService service = new IpLocationService();

    @Test
    void shouldReturnUnknownLocationWhenRequestFails() {

        String location = service.getLocation("invalid-ip");

        assertNotNull(location);
        assertFalse(location.isEmpty());
    }
}