# Configuration - iTools logging

Powsybl's `iTools` command uses [logback](https://logback.qos.ch/) for logging. There are two different ways to configure
the different logging levels:
- a system-wide configuration
- an user-wide configuration

Please refer to the [logback manual](https://logback.qos.ch/manual/index.html) for the available logging options.

## System-wide configuration
The logging configuration file is `<POWSYBL_HOME>/etc/logback-itools.xml`

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

## User-wide configuration
An `iTools` user could have its own logging configuration by creating a `logback-itools.xml` file in the [powsybl
configuration](../README.md) folder. This file is used in priority if it exists and the system-wide configuration is used
otherwise.

## References
See also:
[iTools](../tools/README.md)
