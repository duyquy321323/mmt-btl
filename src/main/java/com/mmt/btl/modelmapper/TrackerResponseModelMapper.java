package com.mmt.btl.modelmapper;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmt.btl.entity.Tracker;
import com.mmt.btl.response.TrackerResponse;

@Component
public class TrackerResponseModelMapper {
    @Autowired
    private ModelMapper modelMapper;

    public TrackerResponse fromTracker(Tracker tracker){
        return modelMapper.map(tracker, TrackerResponse.class);
    }
}