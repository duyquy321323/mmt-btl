package com.mmt.btl.service.impl;

import org.springframework.stereotype.Service;

import com.mmt.btl.repository.FileOrFolderRepository;
import com.mmt.btl.service.FileOrFolderService;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class FileOrFolderServiceImpl implements FileOrFolderService {
    final private FileOrFolderRepository fileOrFolderRepository;
}