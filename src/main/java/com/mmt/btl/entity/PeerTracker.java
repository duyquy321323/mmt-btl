package com.mmt.btl.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.mmt.btl.entity.id.PeerTrackerId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="peer_tracker")
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Setter
@Getter
public class PeerTracker {
    @EmbeddedId
    private PeerTrackerId id;
}