#!/usr/bin/env bash

mvn exec:java -pl sagas -Dexec.mainClass="io.simplesource.example.auction.sagas.SagaCoordinatorApp"
