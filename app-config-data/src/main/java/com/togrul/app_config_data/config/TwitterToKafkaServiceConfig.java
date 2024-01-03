package com.togrul.app_config_data.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "twitter-to-kafka-service")
public class TwitterToKafkaServiceConfig {
    List<String> twitterKeywords;
    String twitterV2BaseUrl;
    String twitterV2RulesBaseUrl;
    String twitterV2BearerToken;
    Boolean enableMockTweets;
    Long mockSleepMs;
    Integer mockMinTweetLength;
    Integer mockMaxTweetLength;
}
