package com.mmt.btl.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
}