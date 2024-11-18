package com.mmt.btl.modelmapper;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmt.btl.entity.FileOrFolder;
import com.mmt.btl.response.FileOrFolderResponse;

@Component
public class FileOrFolderResponseModelMapper {
    @Autowired
    private ModelMapper modelMapper;

    public FileOrFolderResponse fromFileOrFolder(FileOrFolder fileOrFolder){
        FileOrFolderResponse file = modelMapper.map(fileOrFolder, FileOrFolderResponse.class);
        String path = fileOrFolder.getFileName();
        FileOrFolder next = fileOrFolder.getFileOrFolder();
        while(next != null){
            path = next.getFileName() + "/" + path;
            next = next.getFileOrFolder();
        }
        file.setPath(path);
        file.setName(fileOrFolder.getFileName());
        return file;
    }
}