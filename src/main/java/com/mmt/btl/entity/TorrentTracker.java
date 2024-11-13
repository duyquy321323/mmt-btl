package com.mmt.btl.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import com.mmt.btl.entity.id.TorrentTrackerId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TorrentTracker {
    @EmbeddedId
    private TorrentTrackerId id;
}