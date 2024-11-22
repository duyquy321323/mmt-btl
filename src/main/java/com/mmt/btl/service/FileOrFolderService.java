package com.mmt.btl.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartFile;

import com.mmt.btl.request.DownloadRequest;
import com.mmt.btl.response.TorrentResponse;

public interface FileOrFolderService {
    public void upload(HttpServletRequest request, MultipartFile[] files, List<Long> trackerIds) throws NoSuchAlgorithmException, IOException;

    public List<TorrentResponse> getUploadedFile(HttpServletRequest request);

    public List<TorrentResponse> getDownloadFile(HttpServletRequest request);

    public void download(HttpServletRequest request, DownloadRequest downloadRequest);

    public List<TorrentResponse> getDownloadHistoryFile(HttpServletRequest request);
}