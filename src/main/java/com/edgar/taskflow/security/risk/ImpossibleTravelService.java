package com.edgar.taskflow.security.risk;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

@Service
public class ImpossibleTravelService {
	
	public boolean isImpossibleTravel(
	        String lastLocation,
	        String newLocation,
	        LocalDateTime lastActivity
	) {

	    if (lastLocation == null || newLocation == null || lastActivity == null) {
	        return false;
	    }

	    if (lastLocation.equals(newLocation)) {
	        return false;
	    }

	    long minutes =
	            Duration.between(lastActivity, LocalDateTime.now()).toMinutes();

	    return minutes < 30;
	}
}
