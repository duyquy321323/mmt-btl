package com.mmt.btl.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mmt.btl.entity.FileOrFolder;
import com.mmt.btl.entity.Torrent;

public interface FileOrFolderRepository extends JpaRepository<FileOrFolder, Long> {
    public List<FileOrFolder> findAllByTorrent(Torrent torrent);

    @Query("SELECT f FROM FileOrFolder f WHERE f.fileName = :fileName AND f.fileOrFolder = :fileOrFolder")
    public Optional<FileOrFolder> findByFileNameAndFileOrFolder(@Param("fileName") String fileName, @Param("fileOrFolder") FileOrFolder fileOrFolder);
}