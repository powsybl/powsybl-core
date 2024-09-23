# PowSyBl Core


[![Actions Status](https://github.com/powsybl/powsybl-core/workflows/CI/badge.svg)](https://github.com/powsybl/powsybl-core/actions)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=com.powsybl%3Apowsybl-core&metric=coverage)](https://sonarcloud.io/component_measures?id=com.powsybl%3Apowsybl-core&metric=coverage)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=com.powsybl%3Apowsybl-core&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.powsybl%3Apowsybl-core)

[![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/4795/badge)](https://bestpractices.coreinfrastructure.org/projects/4795)
[![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/powsybl/powsybl-core/badge)](https://securityscorecards.dev/viewer/?uri=github.com/powsybl/powsybl-core)

[![MPL-2.0 License](https://img.shields.io/badge/license-MPL_2.0-blue.svg)](https://www.mozilla.org/en-US/MPL/2.0/)
[![Javadocs](https://www.javadoc.io/badge/com.powsybl/powsybl-core.svg?color=blue)](https://www.javadoc.io/doc/com.powsybl/powsybl-core)
[![Slack](https://img.shields.io/badge/slack-powsybl-blueviolet.svg?logo=slack)](https://join.slack.com/t/powsybl/shared_invite/zt-rzvbuzjk-nxi0boim1RKPS5PjieI0rA)

PowSyBl (**Pow**er **Sy**stem **Bl**ocks) is an open source framework written in Java, that makes it easy to write complex
software for power systems’ simulations and analysis. Its modular approach allows developers to extend or customize its
features.

PowSyBl is part of the LF Energy Foundation, a project of The Linux Foundation that supports open source innovation projects
within the energy and electricity sectors.

<p align="center">
<img src="https://raw.githubusercontent.com/powsybl/powsybl-gse/main/gse-spi/src/main/resources/images/logo_lfe_powsybl.svg?sanitize=true" alt="PowSyBl Logo" width="50%"/>
</p>

Read more at https://www.powsybl.org !

This project and everyone participating in it is governed by the [PowSyBl Code of Conduct](https://github.com/powsybl/.github/blob/main/CODE_OF_CONDUCT.md).
By participating, you are expected to uphold this code. Please report unacceptable behavior to [powsybl-tsc@lists.lfenergy.org](mailto:powsybl-tsc@lists.lfenergy.org).

## PowSyBl vs PowSyBl Core

This document describes how to build the code of PowSyBl Core. If you just want to run PowSyBl demos, please visit
https://www.powsybl.org/ where downloads will be available soon. If you want guidance on how to start building your own
application based on PowSyBl, please visit the [tutorials](https://powsybl.readthedocs.io/projects/powsybl-tutorials/) page.

The PowSyBl Core project is not a standalone project. Read on to learn how to modify the core code, be it for fun, for
diagnosing bugs, for improving your understanding of the framework, or for preparing pull requests to suggest improvements!
PowSyBl Core provides library code to build all kinds of applications for power systems: a complete and extendable grid
model, support for common exchange formats, APIs for power simulations an analysis, and support for local or distributed
computations. For deployment, powsybl-core also provides iTools, a tool to build cross-platform integrated command-line
applications. To build cross-platform graphical applications, please visit the PowSyBl GSE repository
https://github.com/powsybl/powsybl-gse page.

## Environment requirements

Powsybl-core project is fully written in Java, so you only need few requirements:
- JDK *(17 or greater)*
- Maven *(3.8.1 or greater)* - you could use the embedded maven wrapper instead if you prefer (see [Using Maven Wrapper](#using-maven-wrapper))

To run all the tests, simply launch the following command from the root of the repository:
```
$> mvn package
```

Modify some existing tests or create your own new tests to experiment with the framework! If it suits you better, import
the project in an IDE and use the IDE to launch your own main classes. If you know java and maven and want to do things
manually, you can also use maven directly to compute the classpath of all the project jars and run anything you want with it.

Read [Contributing.md](https://github.com/powsybl/.github/blob/main/CONTRIBUTING.md) for more in-depth explanations
on how to run code.

Read [Install](#install) to generate an installed iTools distribution, a standalone external folder that contains all
the built objects required to run powsybl programs.

## Install
An iTools distribution can be generated and installed. The installation is a standalone external folder that contains all
the built objects required to run powsybl programs through the itools command-line interface. This repository contains
the `install.sh` script to do so easily. By default, the `install.sh` will compile code and copy the resulting iTools
distribution to the install folder.
```
$> ./install.sh
```

A more detailled description of the install.sh script options follows:

### Targets

| Target | Description |
| ------ | ----------- |
| clean | Clean modules |
| compile | Compile modules |
| package | Compile modules and create a distributable package |
| __install__ | __Compile modules and install it__ |
| docs | Generate the documentation (Javadoc) |
| help | Display this help |

### Options

The install.sh script options are saved in the *install.cfg* configuration file. This configuration file is loaded and
updated each time you use the `install.sh` script.

#### Global options

| Option | Description | Default value |
| ------ | ----------- | ------------- |
| --help | Display this help | |
| --prefix | Set the installation directory | $HOME/powsybl |
| --mvn | Set the maven command to use | mvn | 

### Default configuration file
```
#  -- Global options --
powsybl_prefix=$HOME/powsybl
powsybl_mvn=mvn
```

## Using Maven Wrapper
If you don't have a proper Maven installed, you could use the [Apache Maven Wrapper](https://maven.apache.org/wrapper/)
scripts provided. They will download a compatible maven distribution and use it automatically.

### Configuration
#### Configure the access to the maven distributions
In order to work properly, Maven Wrapper needs to download 2 artifacts: the maven distribution and the maven wrapper
distribution. By default, these are downloaded from the online Maven repository, but you could use an internal repository instead.

##### Using a Maven Repository Manager
If you prefer to use an internal Maven Repository Manager instead of retrieving the artefacts from the internet, you should define the following variable in your environment:
- `MVNW_REPOURL`: the URL to your repository manager (for instance `https://my_server/repository/maven-public`)

Note that if you need to use this variable, it must be set for **each maven command**. Else, the Maven Wrapper will try to
retrieve the maven distribution from the online Maven repository (even if one was already downloaded from another location).

##### Using a proxy to access the Internet
If you don't use an internal Maven Repository, and need to use a proxy to access the Internet, you should:
1. configure the proxy in your terminal (on Linux/MacOS, you can do it via the `http_proxy` and `https_proxy` environment variables).
This is needed to download the Maven Wrapper distribution ;

2. execute **at least once** the following command:
```shell
./mvnw -DproxyHost=XXX -DproxyPort=XXX -Dhttp.proxyUser=XXX -Dhttp.proxyPassword=XXX -Djdk.http.auth.tunneling.disabledSchemes= clean
```
Notes:
- The 4 `XXX` occurrences should be replaced with your configuration;
- The `-Djdk.http.auth.tunneling.disabledSchemes=` option should be left empty;
- Windows users should use `mvnw.cmd` instead of `./mwn`.

This second step is required to download the Maven distribution.

Once both distributions are retrieved, the proxy configuration isn't needed anymore to use `./mvnw` or `mvnw.cmd` commands.


##### Checking your access configuration
You could check your configuration with the following command:
```shell
./mvnw -version
```

If you encounter any problem, you could specify `MVNW_VERBOSE=true` and relaunch the command to have
further information.

#### Configuring `install.sh` to use maven wrapper
To indicate `install.sh` to use Maven Wrapper, you need to configure it with the `--mvn` option:
```shell
./install.sh clean --mvn ./mvnw
```

You can revert this configuration with the following command:
```shell
./install.sh clean --mvn mvn
```

### Usage
Once the configuration is done, you just need to use `./mvnw` instead of `mvn` in your commands.
