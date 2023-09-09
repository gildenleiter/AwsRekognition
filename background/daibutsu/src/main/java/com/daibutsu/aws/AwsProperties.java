package com.daibutsu.aws;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@ConfigurationProperties("aws")
@Component
@Data
public class AwsProperties {
    private String accesskey;
    
    private String secretkey;
}
