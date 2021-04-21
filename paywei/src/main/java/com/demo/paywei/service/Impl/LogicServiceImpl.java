package com.demo.paywei.service.Impl;

import com.demo.paywei.entity.Park;
import com.demo.paywei.mapper.LogicMapper;
import com.demo.paywei.service.LogicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class LogicServiceImpl implements LogicService {

    @Autowired
    private LogicMapper logicMapper;


    @Override
    public Park getParkId(String parkId) {
        return logicMapper.getParkId(parkId);
    }
}
