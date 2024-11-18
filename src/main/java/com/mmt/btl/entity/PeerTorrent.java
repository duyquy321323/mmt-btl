package com.mmt.btl.entity;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.mmt.btl.entity.id.PeerTorrentId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="peer_torrent")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class PeerTorrent {
    @EmbeddedId
    private PeerTorrentId id;

    @Column(name="type_role")
    private String typeRole;
}