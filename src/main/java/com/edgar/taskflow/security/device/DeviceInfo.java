package com.edgar.taskflow.security.device;

public class DeviceInfo {
	
	private String device;
    private String browser;
    private String os;

    public DeviceInfo(String device, String browser, String os) {
        this.device = device;
        this.browser = browser;
        this.os = os;
    }

    public String getDevice() { return device; }
    public String getBrowser() { return browser; }
    public String getOs() { return os; }
}
