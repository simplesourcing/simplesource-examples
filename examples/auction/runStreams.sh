#!/usr/bin/env bash

mvn install -DskipTests
mvn exec:java -Dexec.mainClass="io.simplesource.example.auction.StreamsApplication"
