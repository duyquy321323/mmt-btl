package com.mmt.btl.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mmt.btl.service.TrackerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tracker")
@RequiredArgsConstructor
public class TrackerController {

    final private TrackerService trackerService;

    @GetMapping("/all")
    public ResponseEntity<?> getListTracker(){
        return ResponseEntity.ok(trackerService.getAllTracker());
    }
}