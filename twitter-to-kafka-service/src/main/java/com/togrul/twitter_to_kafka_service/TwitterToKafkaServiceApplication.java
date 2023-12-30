package com.togrul.twitter_to_kafka_service;

import com.togrul.twitter_to_kafka_service.config.TwitterToKafkaServiceConfig;
import com.togrul.twitter_to_kafka_service.runner.StreamRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class TwitterToKafkaServiceApplication {

    private final StreamRunner streamRunner;
    private final TwitterToKafkaServiceConfig twitterToKafkaServiceConfig;

    public static void main(String[] args) {
        SpringApplication.run(TwitterToKafkaServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner run() {
        return args -> {
            log.info("App starts");
            log.info(Arrays.toString(twitterToKafkaServiceConfig.getTwitterKeywords().toArray(new String[0])));
            streamRunner.start();
        };
    }
}
