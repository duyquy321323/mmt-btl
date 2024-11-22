package com.mmt.btl.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ModelMapperConfig extends ModelMapper {
    @Bean
    @Primary
    public ModelMapper mapperConfig() {
        return new ModelMapper();
    }
}