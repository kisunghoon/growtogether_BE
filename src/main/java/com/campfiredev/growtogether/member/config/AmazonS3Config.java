package com.campfiredev.growtogether.member.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Getter
@Setter
@RequiredArgsConstructor
public class AmazonS3Config {
    private final Credentials credentials;

    @Value("${cloud.aws.region}")
    private String region;


    @Bean
    public AmazonS3 amazonS3() {
        if (accessKey == null || secretKey == null || region == null) {
            throw new IllegalArgumentException("AWS Access Key, Secret Key 또는 Region이 설정되지 않았습니다.");
        }

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
      
        return AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
    }
    @Getter
    @Setter
    @Component
    public static class Credentials {
        @Value("${cloud.aws.credentials.access-key}")
        private String accessKey;

        @Value("${cloud.aws.credentials.secret-key}")
        private String secretKey;
    }
}

