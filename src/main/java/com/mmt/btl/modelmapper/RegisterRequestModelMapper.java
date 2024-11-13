package com.mmt.btl.modelmapper;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmt.btl.entity.User;
import com.mmt.btl.request.RegisterRequest;

@Component
public class RegisterRequestModelMapper {
    @Autowired
    private ModelMapper modelMapper;

    public User toUser(RegisterRequest request){
        return modelMapper.map(request, User.class);
    }
}