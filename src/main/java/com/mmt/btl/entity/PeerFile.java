package com.mmt.btl.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.mmt.btl.entity.id.PeerFileId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="peer_file")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class PeerFile {
    @EmbeddedId
    private PeerFileId id;
}