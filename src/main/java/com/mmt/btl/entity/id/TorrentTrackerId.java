package com.mmt.btl.entity.id;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.mmt.btl.entity.Torrent;
import com.mmt.btl.entity.Tracker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TorrentTrackerId implements Serializable {
    @ManyToOne
    @JoinColumn(name="torrent_id")
    private Torrent torrent;

    @ManyToOne
    @JoinColumn(name="tracker_id")
    private Tracker tracker;
}