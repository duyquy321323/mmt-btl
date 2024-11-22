package com.mmt.btl.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import com.mmt.btl.entity.id.PeerId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class Peer {
    @EmbeddedId
    private PeerId id;

    @Column(name="status")
    private Boolean status;

    @OneToMany(mappedBy="id.peer", cascade = {CascadeType.MERGE,CascadeType.PERSIST}, orphanRemoval = true)
    private List<PeerPiece> peerPieces = new ArrayList<>();

    @OneToMany(mappedBy = "id.peer", cascade={CascadeType.MERGE,CascadeType.PERSIST}, orphanRemoval = true)
    private List<PeerTorrent> peerTorrents = new ArrayList<>();

    @OneToMany(mappedBy = "id.peer", cascade={CascadeType.MERGE,CascadeType.PERSIST}, orphanRemoval=true)
    private List<PeerTracker> peerTrackers = new ArrayList<>();
}