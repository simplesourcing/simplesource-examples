#!/usr/bin/env bash

mvn exec:java -pl client -Dexec.mainClass="io.simplesource.example.auction.client.RestApplication"
