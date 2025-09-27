package org.edunex.courseservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    // 1. Pass the properties from application.yml as parameters to the bean method.
    //    Spring will automatically inject them.
    @Bean
    public S3Client s3Client(
            @Value("${spring.aws.region.static}") String region,
            @Value("${spring.aws.credentials.access-key}") String accessKey,
            @Value("${spring.aws.credentials.secret-key}") String secretKey) {

        // 2. Use the injected values to create the credentials
        final AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        final StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

        // 3. Build the S3Client using the region and credentials
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }
}