package com.mmt.btl.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.mmt.btl.entity.id.FilesPieceId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "files_piece")
public class FilesPiece {
    @EmbeddedId
    private FilesPieceId id;

    private Long identity;
}