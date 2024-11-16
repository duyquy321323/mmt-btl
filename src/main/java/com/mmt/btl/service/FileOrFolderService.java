package com.mmt.btl.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartFile;

import com.mmt.btl.response.TorrentResponse;

public interface FileOrFolderService {
    public void upload(HttpServletRequest request, MultipartFile[] files, List<Long> trackerIds) throws NoSuchAlgorithmException, IOException;

    public List<TorrentResponse> getUploadedFile();
}