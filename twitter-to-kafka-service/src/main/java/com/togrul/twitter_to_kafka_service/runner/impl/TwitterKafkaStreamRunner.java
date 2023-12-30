package com.togrul.twitter_to_kafka_service.runner.impl;

import com.togrul.twitter_to_kafka_service.config.TwitterToKafkaServiceConfig;
import com.togrul.twitter_to_kafka_service.listener.TwitterKafkaStatusListener;
import com.togrul.twitter_to_kafka_service.runner.StreamRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("not ${twitter-to-kafka-service.enable-mock-tweets} && not ${twitter-to-kafka-service.enable-v2-tweets}")
public class TwitterKafkaStreamRunner implements StreamRunner {

    private final TwitterKafkaStatusListener twitterKafkaStatusListener;
    private final TwitterToKafkaServiceConfig twitterToKafkaServiceConfig;

    private TwitterStream twitterStream;

    @Override
    public void start() {
        twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(twitterKafkaStatusListener);
        addFilter();
    }

    public void shutdown() {
        if (twitterStream != null) {
            log.info("Closing twitter stream!");
            twitterStream.shutdown();
        }
    }

    private void addFilter() {
        String[] keywords = twitterToKafkaServiceConfig.getTwitterKeywords().toArray(new String[0]);
        FilterQuery filterQuery = new FilterQuery(keywords);
        twitterStream.filter(filterQuery);
        log.info("Started filtering twitter stream for keywords {}", Arrays.toString(keywords));
    }
}
