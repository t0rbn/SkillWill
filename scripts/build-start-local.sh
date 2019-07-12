#!/bin/bash

cd ..

mvn clean install
java -jar target/skillwill.jar --spring.profiles.active=dev
