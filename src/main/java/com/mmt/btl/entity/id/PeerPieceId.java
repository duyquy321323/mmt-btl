package com.mmt.btl.entity.id;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;

import com.mmt.btl.entity.Peer;
import com.mmt.btl.entity.Piece;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class PeerPieceId implements Serializable {
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name="user_agent", referencedColumnName="user_agent"),
        @JoinColumn(name="user_id", referencedColumnName="user_id")
    })
    private Peer peer;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name="piece_id", referencedColumnName="id"),
        @JoinColumn(name="file_or_folder_id", referencedColumnName="file_or_folder_id"),
        @JoinColumn(name="start_offset", referencedColumnName = "start_offset")
    })
    private Piece piece;
}