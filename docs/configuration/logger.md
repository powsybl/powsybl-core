# powsybl logger

Powsybl's iTools command is configured to use [logback](https://logback.qos.ch/) for logging. 
The default logging configuration file is `<POWSYBL_HOME>/etc/logback-itools.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!--

    Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.

-->
<!--Configuration of the command line process log-->
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <Pattern>%d{yyyy-MM-dd_HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</Pattern>
        </encoder>
    </appender>
    <root level="ERROR">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

You can edit the default configuration, for example to modify the debugging level or to make it write to a specific file.
Please refer to the  [logback manual](https://logback.qos.ch/manual/index.html) for the available logging options.
