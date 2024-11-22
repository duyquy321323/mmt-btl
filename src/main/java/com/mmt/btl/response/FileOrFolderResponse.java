package com.mmt.btl.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileOrFolderResponse {
    private String path;
    private Long length;
    private String type;
    private String name;
}