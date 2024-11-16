package com.mmt.btl.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.springframework.data.annotation.CreatedDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class Torrent {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="piece_length")
    private Long pieceLength;

    private String encoding;
    
    @Column(name="create-date")
    private Date createDate;

    @Column(name="create-by")
    private String createBy;

    @Column(name="info-hash")
    private String infoHash;

    @OneToMany(cascade={CascadeType.MERGE,CascadeType.PERSIST}, mappedBy="torrent", orphanRemoval = true)
    private List<FileOrFolder> fileOrFolders;

    @OneToMany(mappedBy = "id.torrent", cascade={CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = true)
    private List<TorrentTracker> torrentTrackers = new ArrayList<>();
}