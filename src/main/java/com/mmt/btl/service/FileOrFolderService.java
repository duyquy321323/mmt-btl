package com.mmt.btl.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.springframework.web.multipart.MultipartFile;

public interface FileOrFolderService {
    public void upload(MultipartFile[] files) throws NoSuchAlgorithmException, IOException;
}