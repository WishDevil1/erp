#!/bin/bash

mvn --file ../../parent-pom/pom.xml -DskipTests --settings ../maven/settings.xml clean install

mvn --file ../../de-metas-common/pom.xml -DskipTests --settings ../maven/settings.xml clean install

mvn --file ../../../backend/pom.xml -DskipTests --settings ../maven/settings.xml clean install
