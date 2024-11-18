package com.mmt.btl.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mmt.btl.entity.FileOrFolder;
import com.mmt.btl.entity.Torrent;

public interface FileOrFolderRepository extends JpaRepository<FileOrFolder, Long> {
    public List<FileOrFolder> findAllByTorrent(Torrent torrent);
}