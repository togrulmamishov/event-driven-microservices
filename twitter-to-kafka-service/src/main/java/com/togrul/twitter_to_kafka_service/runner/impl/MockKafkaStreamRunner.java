package com.togrul.twitter_to_kafka_service.runner.impl;

import com.togrul.twitter_to_kafka_service.config.TwitterToKafkaServiceConfig;
import com.togrul.twitter_to_kafka_service.exception.TwitterToKafkaException;
import com.togrul.twitter_to_kafka_service.listener.TwitterKafkaStatusListener;
import com.togrul.twitter_to_kafka_service.runner.StreamRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("not ${twitter-to-kafka-service.enable-v2-tweets} && ${twitter-to-kafka-service.enable-mock-tweets}")
public class MockKafkaStreamRunner implements StreamRunner {
    private static final Random RANDOM = new Random();
    private static final String[] WORDS = {
            "Lorem", "ipsum", "dolor", "sit", "amet,", "consectetur", "adipiscing", "elit.",
            "Sed", "do", "eiusmod", "tempor", "incididunt", "ut", "labore", "et", "dolore",
            "magna", "aliqua.", "Ut", "enim", "ad", "minim", "veniam,", "quis", "nostrud",
            "exercitation", "ullamco", "laboris", "nisi", "ut", "aliquip", "ex", "ea", "commodo",
            "consequat.", "Duis", "aute", "irure", "dolor", "in", "reprehenderit", "in", "voluptate",
            "velit", "esse", "cillum", "dolore", "eu", "fugiat", "nulla", "pariatur.",
            "Excepteur", "sint", "occaecat", "cupidatat", "non", "proident,", "sunt", "in", "culpa",
            "qui", "officia", "deserunt", "mollit", "anim", "id", "est", "laborum."
    };

    private static final String tweetAsRowJson = """
            {
                "created_at": "{0}",
                "id": "{1}",
                "text": "{2}",
                "user": {"id": "{3}"}
            }
            """;

    private static final String TWITTER_STATUS_DATE_FORMAT = "EEE MMM dd HH:mm:ss zzz yyy";

    private final TwitterKafkaStatusListener twitterKafkaStatusListener;
    private final TwitterToKafkaServiceConfig twitterToKafkaServiceConfig;

    @Override
    public void start() throws TwitterException {
        String[] keywords = twitterToKafkaServiceConfig.getTwitterKeywords().toArray(new String[0]);
        Integer mockMinTweetLength = twitterToKafkaServiceConfig.getMockMinTweetLength();
        Integer mockMaxTweetLength = twitterToKafkaServiceConfig.getMockMaxTweetLength();
        Long mockSleepMs = twitterToKafkaServiceConfig.getMockSleepMs();
        simulateTwitterStream(keywords, mockMinTweetLength, mockMaxTweetLength, mockSleepMs);
    }

    private void simulateTwitterStream(String[] keywords, Integer mockMinTweetLength, Integer mockMaxTweetLength, Long mockSleepMs) {
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                while (true) {
                    String formattedTweetAsRawJson = getFormattedTweet(keywords, mockMinTweetLength, mockMaxTweetLength);
                    Status status = TwitterObjectFactory.createStatus(formattedTweetAsRawJson);
                    twitterKafkaStatusListener.onStatus(status);
                    sleep(mockSleepMs);
                }
            } catch (TwitterException e) {
                log.error("Error creating twitter status!", e);
            }
        });
    }

    void sleep(long sleepMs) {
        try {
            Thread.sleep(sleepMs);
        } catch (InterruptedException e) {
            throw new TwitterToKafkaException("Error while waiting new status to create", e);
        }
    }

    private String getFormattedTweet(String[] keywords, Integer minTweetLength, Integer maxTweetLength) {
        String[] params = new String[]{
                ZonedDateTime.now().format(DateTimeFormatter.ofPattern(TWITTER_STATUS_DATE_FORMAT, Locale.ENGLISH)),
                String.valueOf(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE)),
                getRandomTweetContent(keywords, minTweetLength, maxTweetLength),
                String.valueOf(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE))
        };
        return formatTweetAsJsonWithParams(params);
    }

    private static String formatTweetAsJsonWithParams(String[] params) {
        String tweet = tweetAsRowJson;

        for (int i = 0; i < params.length; i++) {
            tweet = tweet.replace("{" + i + "}", params[i]);
        }
        return tweet;
    }

    private String getRandomTweetContent(String[] keywords, Integer minTweetLength, Integer maxTweetLength) {
        StringBuilder tweet = new StringBuilder();
        int tweetLength = RANDOM.nextInt(maxTweetLength - minTweetLength + 1) + minTweetLength;
        return constructRandomTweet(keywords, tweet, tweetLength);
    }

    private static String constructRandomTweet(String[] keywords, StringBuilder tweet, int tweetLength) {
        for (int i = 0; i < tweetLength; i++) {
            tweet.append(WORDS[RANDOM.nextInt(WORDS.length)]).append(" ");
            if (i == tweetLength / 2) {
                tweet.append(keywords[RANDOM.nextInt(keywords.length)]).append(" ");
            }
        }
        return tweet.toString().trim();
    }
}
