package com.mmt.btl.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToMany;

import com.mmt.btl.entity.id.PieceId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Piece {
    @EmbeddedId
    private PieceId id;

    @Column(name="piece", length=Integer.MAX_VALUE, columnDefinition="LONGBLOB")
    @Lob
    private byte[] piece;

    @OneToMany(mappedBy="id.piece", cascade={CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval=true)
    private List<PeerPiece> peerPieces = new ArrayList<>();

    @Column(name="end_offset", unique=true)
    private Long endOffset;
}