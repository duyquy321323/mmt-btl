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
import javax.persistence.OneToOne;

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

    @OneToOne
    @JoinColumn(name="file_or_folder_id")
    private FileOrFolder fileOrFolder;

    @ManyToMany
    @JoinTable(
        joinColumns=@JoinColumn(name="torrent_id"),
        inverseJoinColumns=@JoinColumn(name="tracker_id"),
        name="torrent_tracker"
    )
    private List<Tracker> trackers = new ArrayList<>();
}