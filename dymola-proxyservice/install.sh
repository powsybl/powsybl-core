#!/bin/bash
#
# Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
#

if [ -z "$DYMOLAHOME" ]; then
    echo "Need to set enviroment variable DYMOLAHOME, pointing to a Dymola installation. Compilation requires two files from DYMOLAHOME/Modelica/Library/java_interface : dymola_interface.jar ,  json-simple-1.1.1.jar"
    exit 1
fi

installDir=./installbins
rm -rf $installDir
mkdir -p $installDir/share $installDir/share/java
mvn -f pom.xml org.apache.maven.plugins:maven-dependency-plugin:2.8:copy-dependencies -DoutputDirectory=$installDir/share/java -DexcludeArtifactIds="slf4j-jdk14,slf4j-log4j12"
mvn org.apache.maven.plugins:maven-dependency-plugin:2.8:copy -Dartifact=eu.itesla_project:dymola-proxyservice:0.1-SNAPSHOT -DoutputDirectory=$installDir/share/java

cp src/main/scripts/service.bat $installDir
cp src/main/scripts/logback-cmdline.xml $installDir/share/java

zip -r dymola_proxyservice_binaries.zip $installDir
rm -rf $installDir
