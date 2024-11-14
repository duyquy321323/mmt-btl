package com.mmt.btl.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Table(name="file_or_folder")
public class FileOrFolder {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="file_name")
    private String fileName;

    @Column(name="length")
    private Long length;

    @Column(name="type")
    private String type;

    @Column(name="pieces", length=Integer.MAX_VALUE)
    private String hashPieces;

    @OneToOne(mappedBy="fileOrFolder", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval=true)
    private Torrent torrent;

    @OneToMany(mappedBy="fileOrFolder", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval=true)
    private List<FileOrFolder> fileOrFolders = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name="folder_parent")
    private FileOrFolder fileOrFolder;

    @OneToMany(mappedBy = "fileOrFolder", cascade={CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval=true)
    private List<FilesPiece> filesPieces = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name="tracker_id")
    private Tracker tracker;
}