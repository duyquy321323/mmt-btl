package com.mmt.btl.entity.id;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;

import com.mmt.btl.entity.FileOrFolder;
import com.mmt.btl.entity.Peer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class PeerFileId implements Serializable {
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name="user_agent", referencedColumnName = "user_agent"),
        @JoinColumn(name="user_id", referencedColumnName="user_id")
    })
    private Peer peer;

    @ManyToOne
    @JoinColumn(name="file_or_folder_id")
    private FileOrFolder fileOrFolder;
}