package com.mmt.btl.controller;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mmt.btl.service.FileOrFolderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileOrFolderController {

    private final FileOrFolderService fileOrFolderService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(HttpServletRequest request, @RequestParam("files") MultipartFile[] files,
            @RequestParam("trackerIds") List<Long> trackerIds) throws NoSuchAlgorithmException, IOException {
        fileOrFolderService.upload(request, files, trackerIds);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}