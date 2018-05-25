#!/bin/sh
mvn clean install -U -Dmaven.test.skip=true
echo "EZMQX build done"

