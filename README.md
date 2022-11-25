# PowSyBl Core

[![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/4795/badge)](https://bestpractices.coreinfrastructure.org/projects/4795)
[![Actions Status](https://github.com/powsybl/powsybl-core/workflows/CI/badge.svg)](https://github.com/powsybl/powsybl-core/actions)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=com.powsybl%3Apowsybl-core&metric=coverage)](https://sonarcloud.io/component_measures?id=com.powsybl%3Apowsybl-core&metric=coverage)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=com.powsybl%3Apowsybl-core&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.powsybl%3Apowsybl-core)
[![MPL-2.0 License](https://img.shields.io/badge/license-MPL_2.0-blue.svg)](https://www.mozilla.org/en-US/MPL/2.0/)
[![Join the community on Spectrum](https://withspectrum.github.io/badge/badge.svg)](https://spectrum.chat/powsybl)
[![Slack](https://img.shields.io/badge/slack-powsybl-blueviolet.svg?logo=slack)](https://join.slack.com/t/powsybl/shared_invite/zt-rzvbuzjk-nxi0boim1RKPS5PjieI0rA)
[![Javadocs](https://www.javadoc.io/badge/com.powsybl/powsybl-core.svg?color=blue)](https://www.javadoc.io/doc/com.powsybl/powsybl-core)

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
application based on PowSyBl, please visit the http://www.powsybl.org/docs/tutorials/ page.

The PowSyBl Core project is not a standalone project. Read on to learn how to modify the core code, be it for fun, for
diagnosing bugs, for improving your understanding of the framework, or for preparing pull requests to suggest improvements!
PowSyBl Core provides library code to build all kinds of applications for power systems: a complete and extendable grid
model, support for common exchange formats, APIs for power simulations an analysis, and support for local or distributed
computations. For deployment, powsybl-core also provides iTools, a tool to build cross-platform integrated command-line
applications. To build cross-platform graphical applications, please visit the PowSyBl GSE repository
https://github.com/powsybl/powsybl-gse page.

## Environment requirements

Powsybl-core project is fully written in Java, so you only need few requirements:
- JDK *(11 or greater)*
- Maven *(3.3.9 or greater)*

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

### Default configuration file
```
#  -- Global options --
powsybl_prefix=$HOME/powsybl
```
