package com.togrul.twitter_to_kafka_service.runner.impl;

import com.togrul.twitter_to_kafka_service.config.TwitterToKafkaServiceConfig;
import com.togrul.twitter_to_kafka_service.runner.StreamRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("${twitter-to-kafka-service.enable-v2-tweets} && not ${twitter-to-kafka-service.enable-mock-tweets}")
public class TwitterV2KafkaStreamRunner implements StreamRunner {

    private final TwitterToKafkaServiceConfig twitterToKafkaServiceConfig;
    private final TwitterV2StreamHelper twitterV2StreamHelper;

    @Override
    public void start() {
        String bearerToken = twitterToKafkaServiceConfig.getTwitterV2BearerToken();
        if (bearerToken == null) {
            log.error("Bearer token is not provided");
            throw new RuntimeException("Bearer token is not provided");
        }
        try {
            twitterV2StreamHelper.setupRules(bearerToken, getRules());
            twitterV2StreamHelper.connectStream(bearerToken);
        } catch (IOException | URISyntaxException e) {
            log.error("Error streaming tweets!", e);
            throw new RuntimeException("Error streaming tweets!", e);
        }
    }

    private Map<String, String> getRules() {
        List<String> keywords = twitterToKafkaServiceConfig.getTwitterKeywords();
        Map<String, String> rules = new HashMap<>();
        keywords.forEach(keyword -> rules.put(keyword, "Keyword: " + keyword));
        log.info("Created filter for twitter stream keywords: {}", keywords);
        return rules;
    }
}
