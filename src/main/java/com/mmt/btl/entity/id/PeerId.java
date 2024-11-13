package com.mmt.btl.entity.id;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.mmt.btl.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PeerId implements Serializable {

    @Column(name="user_agent")
    private String userAgent;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;
}