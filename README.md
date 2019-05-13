# PowSyBl Core

[![Build Status](https://api.travis-ci.com/powsybl/powsybl-core.svg?branch=master)](https://travis-ci.com/powsybl/powsybl-core)
[![Build status](https://ci.appveyor.com/api/projects/status/76o2bbmsewpbpr97/branch/master?svg=true)](https://ci.appveyor.com/project/powsybl/powsybl-core/branch/master)
[![Coverage Status](https://coveralls.io/repos/github/powsybl/powsybl-core/badge.svg?branch=master)](https://coveralls.io/github/powsybl/powsybl-core?branch=master)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=com.powsybl%3Apowsybl-core&metric=coverage)](https://sonarcloud.io/component_measures?id=com.powsybl%3Apowsybl-core&metric=coverage)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=com.powsybl%3Apowsybl-core&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.powsybl%3Apowsybl-core)
[![MPL-2.0 License](https://img.shields.io/badge/license-MPL_2.0-blue.svg)](https://www.mozilla.org/en-US/MPL/2.0/)
[![Join the community on Spectrum](https://withspectrum.github.io/badge/badge.svg)](https://spectrum.chat/powsybl)
[![Javadocs](https://www.javadoc.io/badge/com.powsybl/powsybl-core.svg?color=blue)](https://www.javadoc.io/doc/com.powsybl/powsybl-core)

PowSyBl (**Pow**er **Sy**stem **Bl**ocks) is an open source framework written in Java, that makes it easy to write complex software for power systems’ simulations and analysis. Its modular approach allows developers to extend or customize its features.

PowSyBl is part of the LF Energy Foundation, a project of The Linux Foundation that supports open source innovation projects within the energy and electricity sectors.

<p align="center">
<img src="https://raw.githubusercontent.com/powsybl/powsybl-gse/master/gse-spi/src/main/resources/images/logo_lfe_powsybl.svg?sanitize=true" alt="PowSyBl Logo" width="50%"/>
</p>

Read more at https://www.powsybl.org !

This project and everyone participating in it is governed by the [PowSyBl Code of Conduct](https://github.com/powsybl/.github/blob/master/CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code. Please report unacceptable behavior to [powsybl.ddl@rte-france.com](mailto:powsybl.ddl@rte-france.com).

## PowSyBl vs PowSyBl Core

This document describes how to build the code of PowSyBl Core. If you just want to run PowSyBl demos, please visit https://www.powsybl.org/ where downloads will be available soon. If you want guidance on how to start building your own application based on PowSyBl, please visit the http://www.powsybl.org/docs/tutorials/ page.

The PowSyBl Core project is not a standalone project. Read on to learn how to modify the core code, be it for fun, for diagnosing bugs, for improving your understanding of the framework, or for preparing pull requests to suggest improvements! PowSyBl Core provides library code to build all kinds of applications for power systems: a complete and extendable grid model, support for common exchange formats, APIs for power simulations an analysis, and support for local or distributed computations. For deployment, powsybl-core also provides iTools, a tool to build cross-platform integrated command-line applications. To build cross-platform graphical applications, please visit the PowSyBl GSE repository https://github.com/powsybl/powsybl-gse page.

## Environment requirements

Most of the powsybl-core project is written in Java, with additional C++ components. Unless you are actually using the C++ components, it is not needed to build them and you can start to experiment and run code quickly with the [Simple Java Build](#simple-java-build). If you want to use the C++ components, refer to the [Full Project Build](#full-project-build).

## Simple Java Build

  * JDK *(1.8 or greater)*
  * Maven *(3.3.9 or greater)*

To run all the tests, simply launch the following command from the root of the repository:
```
$> mvn package
```

Modify some existing tests or create your own new tests to experiment with the framework! If it suits you better, import the project in an IDE and use the IDE to launch your own main classes. If you know java and maven and want to do things manually, you can also use maven directly to compute the classpath of all the project jars and run anything you want with it.

Read [Contributing.md](https://github.com/powsybl/.github/blob/master/CONTRIBUTING.md) for more in-depth explanations on how to run code.

Read [Install](#install) to generate an installed iTools distribution, a standalone external folder that contains all the built objects required to run powsybl programs.

## Full Project Build
In order to fully build the project (i.e. Java and C++ modules), in addition to the java requirements, you need:
  * CMake *(2.6 or greater)*
  * A recent C++ compiler (GNU g++ or Clang)
  * OpenMPI *(1.8.3 or greater)*
  * Some development packages (zlib, bzip2)

### OpenMPI (required)
In order to support the MPI modules, you need to compile and install the [OpenMPI](https://www.open-mpi.org/) library.
```
$> wget http://www.open-mpi.org/software/ompi/v1.8/downloads/openmpi-1.8.3.tar.bz2
$> tar xjf openmpi-1.8.3.tar.bz2
$> cd openmpi-1.8.3
$> ./configure --prefix=<INSTALL_DIR> --enable-mpi-thread-multiple
$> make install
$> export PATH=$PATH:<INSTALL_DIR>/bin
$> export LD_LIBRARY_PATH=<INSTALL_DIR>/lib:$LD_LIBRARY_PATH
```

### zlib (required)
In order to build the Boost external package, you have to install [zlib](http://www.zlib.net/) library.
```
$> yum install zlib-devel
```

### bzip2 (required)
In order to build the Boost external package, you have to install [bzip](http://www.bzip.org/) library.
```
$> yum install bzip2-devel
```

If you want, you can now run cmake manually to compile the code. However, an easier alternative is to use the install.sh script (with C++ modules) described below. The C++ compilation will be done automatically as part of the installation process.

## Install
An iTools distribution can be generated and installed. The installation is a standalone external folder that contains all the built objects required to run powsybl programs through the itools command-line interface. This repository contains the install.sh script to do so easily. By default, the install.sh will download dependencies from the Internet, compile code and finally copy the resulting iTools distribution to the install folder.

For most users, create the java only distribution with:
```
$> ./install.sh --without-cpp
```
This will run the maven build and copy the result to the install folder.

In the case you do require the C++ modules, run:
```
$> ./install.sh
```
This will run both the C++ build and the java build and copy their results to the install folder.

A more detailled description of the install.sh script options follows:

### Targets

| Target | Description |
| ------ | ----------- |
| clean | Clean modules |
| clean-thirdparty | Clean the thirdparty libraries |
| compile | Compile modules |
| package | Compile modules and create a distributable package |
| __install__ | __Compile modules and install it__ |
| docs | Generate the documentation (Doxygen/Javadoc) |
| help | Display this help |

### Options

The install.sh script options are saved in the *install.cfg* configuration file. This configuration file is loaded and updated
each time you use the install.sh script.

#### Global options

| Option | Description | Default value |
| ------ | ----------- | ------------- |
| --help | Display this help | |
| --prefix | Set the installation directory | $HOME/powsybl |
| --without-cpp | Disable C++ modules compilation | |

#### Third-parties

| Option | Description | Default value |
| ------ | ----------- | ------------- |
| --with-thirdparty | Enable the compilation of thirdparty libraries | |
| --without-thirdparty | Disable the compilation of thirdparty libraries | |
| --thirdparty-prefix | Set the thirdparty installation directory | $HOME/powsybl_thirdparty |
| --thirdparty-download | Sets false to compile thirdparty libraries from a local repository | true |
| --thirdparty-packs | Sets the thirdparty libraries local repository | $HOME/powsybl_packs |

### Default configuration file
```
#  -- Global options --
powsybl_prefix=$HOME/powsybl
powsybl_cpp=false

#  -- Thirdparty libraries --
thirdparty_build=true
thirdparty_prefix=$HOME/powsybl_thirdparty
thirdparty_download=true
thirdparty_packs=$HOME/powsybl_packs
```
