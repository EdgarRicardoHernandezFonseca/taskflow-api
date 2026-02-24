package com.edgar.taskflow.auth;

import lombok.Data;

@Data
public class RefreshRequest {

	private String refreshToken;
}
