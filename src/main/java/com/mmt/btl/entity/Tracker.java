package com.mmt.btl.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class Tracker {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="host_name")
    private String hostname;

    @Column(name="port")
    private Long port;

    @OneToMany(mappedBy = "id.tracker", cascade={CascadeType.MERGE,CascadeType.PERSIST}, orphanRemoval=true)
    private List<PeerTracker> peerTrackers = new ArrayList<>();

    @OneToMany(mappedBy="id.tracker", cascade={CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = true)
    private List<TorrentTracker> torrentTrackers = new ArrayList<>();
}