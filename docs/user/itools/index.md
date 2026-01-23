# iTools

```{toctree}
---
maxdepth: 1
hidden: true
---
action-simulator.md
cim_anonymizer.md
compare-security-analysis-results.md
convert_network.md
dynamic-security-analysis.md
dynamic-simulation.md
list-dynamic-simulation-models.md
loadflow.md
loadflow-validation.md
run-script.md
security-analysis.md
sensitivity-computation.md
```

The `iTools` script provides a command-line interface to interact with PowSyBl, available under Linux and Windows (macOS is not supported yet).

An `iTools` package is constituted of:
- a `bin` directory containing the executable scripts and the binaries
- an `etc` directory containing the configuration and the `XIIDM` schemas
- a `lib` directory containing C++ libraries
- a `share/java` directory containing Java libraries

## Installation

It is possible to install a basic PowSyBl distribution and to start running iTools commands from the binaries or from the sources

### Install from the binaries

To use the executable tool provided with the `powsybl-distribution` repository, follow these steps:
- Download the zip folder from the latest released version of [powsybl-distribution](https://github.com/powsybl/powsybl-distribution/releases)
- Unzip the downloaded package
- You can then add `<INSTALL_DIR>/powsybl-distribution-<LATEST_VERSION>/bin` to your environment variable `PATH`.

### Install from the sources

To generate the executable tool from the sources, follow these steps:
- Download the [powsybl-distribution](https://github.com/powsybl/powsybl-distribution) sources
- Checkout the latest stable version by referencing the tag
- Generate the executable with the maven command

```
$ git clone https://github.com/powsybl/powsybl-distribution.git
$ cd powsybl-distribution
$ git checkout tags/<LATEST_RELEASE_TAG> -b latest-release
$ mvn clean package
```

The distribution is generated in the `target` folder.  
- Add `<INSTALL_DIR>/powsybl-distribution-<LATEST_VERSION>/bin` to your environment variable `PATH`.

### Test your installation

Launch the `itools --help` command in your terminal to check that everything went smoothly.

```
$> itools --help
usage: itools [OPTIONS] COMMAND [ARGS]

Available options are:
    --config-name <CONFIG_NAME>   Override configuration file name

Available commands are:

Computation:
    compare-security-analysis-results        Compare security analysis results
    loadflow                                 Run loadflow
    loadflow-validation                      Validate load-flow results of a network
    security-analysis                        Run security analysis

Data conversion:
    convert-network                          convert a network from one format to another

Misc:
    plugins-info                             List the available plugins

Script:
    run-script                               run script (only groovy is supported)

```

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

`--config-name`<br>
Use this option to overload the default base name for the configuration file. It overrides the [powsybl_config_name](#powsybl_config_name) property defined in the `itools.conf` file.

(itools-configuration)=
## Configuration

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

You can set a default configuration `config.yml` by copying the provided configuration file in your `<HOME>/.itools` repository (note that you will need to create this repository if it does not exist):

```
$ mkdir <HOME>/.itools
$ cp <INSTALL_DIR>/resources/config/config.yml <HOME>/.itools/config.yml
```

This step is not mandatory **if you already have a custom configuration file and the necessary configuration modules are filled**.

**powsybl_config_dirs:**<br>
This is an optional property that defines the list of the folders where the configuration files are located. If this property is not set, the configuration files are read from `<USER_HOME>/.itools` and `<ITOOLS_PREFIX>/etc` folders. Note that the order of the folder is really import as the PowSyBl configuration is stackable.

(powsybl_config_name)=
**powsybl_config_name:**<br>
This is an optional property that defines the base name of the configuration files. The default value for this property is `config`.

**java_xmx:**<br>
This is an optional property that defines the maximum size of the memory allocation pool of the JVM. The default value for this property is 8 gigabytes.

## Logging
The `iTools` script uses [logback](https://logback.qos.ch/) as a logging framework. To configure the logging framework, edit the `<ITOOLS_HOME>/etc/logback-itools.xml` configuration file. Please refer to the [logback manual](https://logback.qos.ch/manual/index.html) for the available logging options.

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

(itools-available-commands)=
## Available commands
The `iTools` script relies on a plugin mechanism: the commands are discovered at runtime and depend on the jars present in the `share/java` folder.

| Command                                                                     | Theme           | Description                                   |
|-----------------------------------------------------------------------------|-----------------|-----------------------------------------------|
| [action-simulator](./action-simulator.md)                                   | Computation     | Run a security analysis with remedial actions |
| [cim-anonymizer](cim_anonymizer.md)                                         | Data conversion | Anonymize CIM files                           |
| [compare-security-analysis-results](./compare-security-analysis-results.md) | Computation     | Compare security analysis results             |
| [convert-network](convert_network.md)                                       | Data conversion | Convert a grid file from a format to another  |
| [dynamic-simulation](dynamic-simulation.md)                                 | Computation     | Run a dynamic simulation                      |
| [loadflow](loadflow.md)                                                     | Computation     | Run a power flow simulation                   |
| [loadflow-validation](loadflow-validation.md)                               | Computation     | Validate load flow results on a network       |
| [run-script](run-script.md)                                                 | Script          | Run a script on top of PowSyBl                |
| [security-analysis](./security-analysis.md)                                 | Computation     | Run a security analysis                       |
| [dynamic-security-analysis](./dynamic-security-analysis.md)                 | Computation     | Run a dynamic security analysis               |
| [sensitivity-computation](sensitivity-computation.md)                       | Computation     | Run a sensitivity analysis                    |

## Going further
The following links could also be useful:
- [Bundle an iTools package](inv:powsybltutorials:*:*#itools/itools-packager): Learn how to use the `itools-packager` maven plugin
- [Create an iTools command](inv:powsybltutorials:*:*#itools/itools-command): Learn how to create your own `iTools` command in Java
