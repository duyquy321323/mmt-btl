package com.mmt.btl.entity.id;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.mmt.btl.entity.FileOrFolder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PieceId implements Serializable {
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "file_or_folder_id", foreignKey=@ForeignKey(name="FK_piece_file_or_folder_id"))
    private FileOrFolder fileOrFolder;

    @Column(name="start_offset")
    private Long startOffset;

    @Column(name="end_offset")
    private Long endOffset;
}