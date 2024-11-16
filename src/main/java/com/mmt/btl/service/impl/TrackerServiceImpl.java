package com.mmt.btl.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.mmt.btl.modelmapper.TrackerResponseModelMapper;
import com.mmt.btl.repository.TrackerRepository;
import com.mmt.btl.response.TrackerResponse;
import com.mmt.btl.service.TrackerService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional
public class TrackerServiceImpl implements TrackerService {

    final private TrackerRepository trackerRepository;

    final private TrackerResponseModelMapper trackerResponseModelMapper;

    @Override
    public List<TrackerResponse> getAllTracker() {
        return trackerRepository.findAll().stream().map(it -> trackerResponseModelMapper.fromTracker(it))
                .collect(Collectors.toList());
    }

}