#!/bin/bash
cd `dirname $0`/..

java -jar target/solace-qookeeper-1.0-SNAPSHOT-jar-with-dependencies.jar \
    src/main/resources/solconfig1.yml \
    src/main/resources/qkconfig1.yml

