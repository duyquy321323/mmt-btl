package com.mmt.btl.response;

import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TorrentResponse {
    private List<FileOrFolderResponse> fileOrFolders;
    private List<String> announce;
    private Map<String, Object> treeFiles;
    private String pieces;
    private String hashInfo;
    private String encoding;
    private Date createDate;
    private String createBy;
}