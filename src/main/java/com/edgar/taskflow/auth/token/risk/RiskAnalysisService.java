package com.edgar.taskflow.auth.token.risk;

import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.security.device.DeviceInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RiskAnalysisService {
	
	public void analyzeLogin(User user,
            String ip,
            String location,
            DeviceInfo device) {

	// aquí puedes integrar:
	// ImpossibleTravelService
	// LoginAttemptService
	// Device change detection
	
	}
}
