package com.mmt.btl.modelmapper;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmt.btl.entity.User;
import com.mmt.btl.response.LoginResponse;

@Component
public class UserModelMapper {

    @Autowired
    private ModelMapper modelMapper;

    public LoginResponse toLoginResponse(User user){
        return modelMapper.map(user, LoginResponse.class);
    }
}