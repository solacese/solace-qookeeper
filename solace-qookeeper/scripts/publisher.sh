#!/bin/bash
cd `dirname $0`/..

java -cp target/solace-qookeeper-1.0-SNAPSHOT-jar-with-dependencies.jar \
    com.solace.qk.TestOrderProducer \
    src/main/resources/solconfig1.yml \
    src/main/resources/qkconfig1.yml

