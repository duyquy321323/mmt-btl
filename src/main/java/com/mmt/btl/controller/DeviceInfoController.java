package com.mmt.btl.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeviceInfoController {

    @GetMapping("/device-info")
    public String getDeviceInfo(HttpServletRequest request) {
        // Lấy thông tin User-Agent từ request header
        String userAgent = request.getHeader("User-Agent");

        // Xử lý thông tin User-Agent để xác định thiết bị
        return "User-Agent: " + userAgent;
    }
}