package com.mmt.btl.response;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TorrentResponse {
    private List<FileOrFolderResponse> fileOrFolders;
    private List<String> trackerUrl;
    private String encoding;
    private Date createDate;
    private String createBy;
}