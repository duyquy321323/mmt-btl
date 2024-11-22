package com.mmt.btl.request;

import java.util.List;
import java.util.Map;

import com.mmt.btl.response.FileOrFolderResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DownloadRequest {
    private List<FileOrFolderResponse> fileOrFolders;
    private Map<String, Object> treeFiles;
    private String pieces;
    private String hashInfo;
    private String path;
}