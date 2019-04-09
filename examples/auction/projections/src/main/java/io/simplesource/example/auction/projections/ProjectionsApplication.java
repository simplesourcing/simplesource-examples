package io.simplesource.example.auction.projections;

import io.simplesource.example.auction.projections.account.AccountProjectionStreamApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This app is responsible for creating projections from events.
 * Projections are written to Kafka topics where they are streamed to the read store
 * (in our case MongoDB) via Kafka-Connect.
 */
public class ProjectionsApplication {

    private static final Logger logger = LoggerFactory.getLogger(ProjectionsApplication.class);

    public static void main(String[] args) {
        new AccountProjectionStreamApp().start();
        logger.info("Account projections started");
    }
}
