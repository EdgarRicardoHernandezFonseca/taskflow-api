package com.edgar.taskflow.auth.session;

import com.edgar.taskflow.auth.dto.ActiveSessionResponse;
import com.edgar.taskflow.entity.RefreshToken;
import org.springframework.stereotype.Component;

@Component
public class SessionMapper {
	
	 public ActiveSessionResponse toResponse(RefreshToken token) {

	        ActiveSessionResponse response = new ActiveSessionResponse();

	        response.setDeviceName(token.getDeviceName());
	        response.setBrowser(token.getBrowser());
	        response.setLocation(token.getLocation());
	        response.setLastActivity(token.getLastActivity());

	        return response;
	    }
}
