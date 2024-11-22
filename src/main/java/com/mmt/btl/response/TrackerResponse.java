package com.mmt.btl.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrackerResponse {
    private Long id;
    private String hostname;
    private Long port;
}