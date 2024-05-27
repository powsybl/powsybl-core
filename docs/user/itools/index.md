# iTools

```{toctree}
---
maxdepth: 2
---

convert_network.md
```

The `iTools` script provides a command-line interface to interact with PowSyBl, available under Linux and Windows (MacOS is not supported yet).

An `iTools` package is constituted of:
- a `bin` directory containing the executable scripts and the binaries
- an `etc` directory containing the configuration and the `XIIDM` schemas
- a `lib` directory containing C++ libraries
- a `share/java` directory containing Java libraries

## Usage
The `iTools` script is available in the `bin` directory.
```
$> ./bin/itools
usage: itools [OPTIONS] COMMAND [ARGS]

Available options are:
    --config-name <CONFIG_NAME>   Override configuration file name

Available commands are:
...
```

**\-\-config-name**  
Use this option to overload the default base name for the configuration file. It overrides the [powsybl_config_name](#powsybl_config_name) property defined in the `itools.conf` file.

### Configuration
The `iTools` script reads its configuration from the `<ITOOLS_PREFIX>/etc/itools.conf` [property file](https://en.wikipedia.org/wiki/.properties). The properties defined in this file are used to configure the Java Virtual Machine.

**Example of itools.conf file:**
```
# PowSyBl configuration directories
#powsybl_config_dirs=

# PowSyBl configuration base name
powsybl_config_name=config

# Maximum size of the Java memory allocation pool
java_xmx=8G
```

**powsybl_config_dirs:** This is an optional property that defines the list of the folders where the configuration files are located. If this property is not set, the configuration files are read from `<USER_HOME>/.itools` and `<ITOOLS_PREFIX>/etc` folders. Note that the order of the folder is really import as the PowSyBl configuration is [stackable]().

<a class="heading" id="powsybl_config_name"/>**powsybl_config_name:** This is an optional property that defines the base name of the configuration files. The default value for this property is `config`.

**java_xmx:** This is an optional property that defines the maximum size of the memory allocation pool of the JVM. The default value for this property is 8 gigabytes.

The list of the deprecated properties is available [here]()

### Logging
The `iTools` script uses [logback](https://logback.qos.ch/) as logging framework. To configure the logging framework, edit the `<ITOOLS_HOME>/etc/logback-itools.xml` configuration file. Please refer to the [logback manual](https://logback.qos.ch/manual/index.html) for the available logging options.

Sometimes, it could be useful for a user to have its own logging configuration to filter unexpected logs or to have more details for some features. The simplest way to proceed is to copy the global configuration file in the `<USER_HOME>/.itools` folder and then customize it.

**Example of logback-itools.xml:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
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

## Available commands
The `iTools` script relies on a [plugin mechanism](): the commands are discovered at runtime and depend on the jars present in the `share/java` folder.

| Command                                                                   | Theme | Description |
|---------------------------------------------------------------------------| ----- | ----------- |
| [action-simulator](action-simulator.md)                                   | Computation | Run a security analysis with remedial actions |
| [cim-anonymizer](cim-anonymizer.md)                                       | Data conversion | Anonymize CIM files |
| [compare-security-analysis-results](compare-security-analysis-results.md) | Computation | Compare security analysis results |
| [convert-network](convert_network.md)                                     | Data conversion | Convert a grid file from a format to another |
| [dynamic-simulation](dynamic-simulation.md)                               | Computation | Run a dynamic simulation |
| [loadflow](loadflow.md)                                                   | Computation | Run a power flow simulation |
| [loadflow-validation](loadflow-validation.md)                             | Computation | Validate load flow results on a network |
| [run-script](run-script.md)                                               | Script | Run a script on top of PowSyBl | 
| [security-analysis](security-analysis.md)                                 | Computation | Run a security analysis |
| [sensitivity-computation](sensitivity-computation.md)                     | Computation | Run a sensitivity analysis |

## Going further
The following links could also be useful:
- [Bundle an iTools package](../../developer/tutorials/itools-packager.md): Learn how to use the `itools-packager` maven plugin
- [Create an iTools command](../../developer/tutorials/itools-command.md): Learn how to create your own `iTools` command in Java
