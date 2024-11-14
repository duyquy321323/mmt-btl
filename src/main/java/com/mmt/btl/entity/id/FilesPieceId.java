package com.mmt.btl.entity.id;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.mmt.btl.entity.FileOrFolder;
import com.mmt.btl.entity.Piece;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FilesPieceId {
    @ManyToOne
    @JoinColumn(name="file_or_folder_id")
    private FileOrFolder fileOrFolder;

    @ManyToOne
    @JoinColumn(name = "piece_hash")
    private Piece piece;
}