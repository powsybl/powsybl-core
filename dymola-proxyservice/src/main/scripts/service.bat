REM Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
REM This Source Code Form is subject to the terms of the Mozilla Public
REM License, v. 2.0. If a copy of the MPL was not distributed with this
REM file, You can obtain one at http://mozilla.org/MPL/2.0/.

SET JAVA_HOME=C:\Users\iTesla\Downloads\jdk1.8.0_31
SET PATH=%JAVA_HOME%\bin;%PATH%
java -cp "./share/java/*" -Dlogback.configurationFile=./share/java/logback-cmdline.xml eu.itesla_project.dymola.service.ServiceMain "./server" 9000 30 0.0.0.0 8080 30 true
