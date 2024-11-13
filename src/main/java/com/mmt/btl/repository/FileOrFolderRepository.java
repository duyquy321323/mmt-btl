package com.mmt.btl.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mmt.btl.entity.FileOrFolder;

public interface FileOrFolderRepository extends JpaRepository<FileOrFolder, Long> {
}