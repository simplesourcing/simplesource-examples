#!/usr/bin/env bash

mvn exec:java -pl server -Dexec.mainClass="io.simplesource.example.auction.server.StreamsApplication"
