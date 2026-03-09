package com.edgar.taskflow.auth.service;

import org.springframework.stereotype.Service;

@Service
public class MfaService {

    public boolean isStepUpRequired(boolean riskDetected){
        return riskDetected;
    }

}