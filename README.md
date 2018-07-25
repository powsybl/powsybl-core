

# Powsybl-Core: Installation guide  

[![Build Status](https://travis-ci.org/powsybl/powsybl-core.svg?branch=master)](https://travis-ci.org/powsybl/powsybl-core)
[![Build status](https://ci.appveyor.com/api/projects/status/76o2bbmsewpbpr97/branch/master?svg=true)](https://ci.appveyor.com/project/powsybl/powsybl-core/branch/master)
[![Coverage Status](https://coveralls.io/repos/github/powsybl/powsybl-core/badge.svg?branch=master)](https://coveralls.io/github/powsybl/powsybl-core?branch=master)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=com.powsybl%3Apowsybl-core&metric=coverage)](https://sonarcloud.io/component_measures?id=com.powsybl%3Apowsybl-core&metric=coverage)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=com.powsybl%3Apowsybl-core&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.powsybl%3Apowsybl-core)
[![MPL-2.0 License](https://img.shields.io/badge/license-MPL_2.0-blue.svg)](https://www.mozilla.org/en-US/MPL/2.0/)

http://www.powsybl.com

## Overview
Powsybl (Power System Blocks) is an open-source Java framework dedicated to building power system oriented software. 
Its source code is distributed under the Mozilla Public License 2.0 and it is hosted on GitHub: https://github.com/powsybl .
  
The project powsybl-core provides the framework's core functionalities. It is designed in a modular approach, 
to allow other projects to extend its features or to modify the default behaviours.

* IIDM (iTesla Internal Data Model) APIs: able to represent power grid models (substations, voltage levels, lines, two and three windings transformers, generator, etc.)
* Data converters APIs: currently, to load from Entso-E CIM/CGMES, UCTE, AMPL, XIIDM XML and save to XIIDM XML, AMPL 
* Contingencies APIs
* iAL iTesla Action Language: a domain specific language for action simulation on the network
* Load-Flow & Simulation APIs
* Computation modules, including distribution functionalities based on MPI (Message Passing Interface)
* Standard Security Analysis APIs
* AFS (Application File System)
* Scripting (Groovy)
* Tools: command line tools interface

This guide explains how to build powsybl-core sources into a powsybl binary installation.

## Environment requirements
Linux is required, this guide covers these distributions  
 * CentOS v6.x / v7.x
 
In order to build the project, you need:
  * [Oracle JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) *(1.8)*
  * [Maven](https://maven.apache.org/download.cgi) (>= 3.5.3) 
  * CMake *(>= 2.6)*
  * A recent C++ compiler (GNU g++ or Clang)
  * [OpenMPI](https://www.open-mpi.org/) *(tested with 1.8.3)*
  * Some development libraries (e.g. [zlib](http://www.zlib.net/) and [bzip](http://www.bzip.org/))

### Build tools and libraries installation
Root privileges are required to install these packages (used to build some external modules, e.g. Boost):  
```
$> sudo yum group install "Development Tools" 
$> sudo yum install git cmake icu tar wget bzip2-devel zlib-devel

```

### OpenMPI (required)
In order to use the MPI based computation layer, you need to compile and install the OpenMPI library.
Building from sources is required because of the non-standard compilation configuration (with respect to the packages distributed via the standard OS install tool, like 'yum')
```
$> wget http://www.open-mpi.org/software/ompi/v1.8/downloads/openmpi-1.8.3.tar.bz2
$> tar xjf openmpi-1.8.3.tar.bz2
$> cd openmpi-1.8.3
$> ./configure --prefix=<INSTALL_DIR> --enable-mpi-thread-multiple
$> make install
```
Then, add these exports to your $HOME/.bashrc
```
$> export PATH=$PATH:<INSTALL_DIR>/bin
$> export LD_LIBRARY_PATH=<INSTALL_DIR>/lib:$LD_LIBRARY_PATH
```

### JDK and Maven (required)
JDK and maven tools commands are expected to be available in the PATH 

## Clone the powsybl-core repository
These instructions explain how to download the project using a command line based git client, but a git client integrated in an IDE might also be used to download the project code and to build a specific branch (e.g. a release branch)
A git client is also strongly suggested for any development activities.  Alternatively, a sources .zip archive can be downloaded from the [project's GitHub page](https://github.com/powsybl/powsybl-core).

To clone the latest powsybl-core repository
```
$> git clone https://github.com/powsybl/powsybl-core.git
```
Note: git clone creates a directory powsybl-core. After cloning, you may switch to and build a specific branch RELEASE_BRANCH by
```
$> cd powsyble-core
$> git checkout RELEASE_BRANCH
```


## Compile and install powsybl-core 
To easily compile powsybl-core, you can use the `Install.sh` script, stored in the root project's directory:
```
$> ./install.sh
```

By default, the install script will:
  * download and compile all external packages from the Internet
  * compile C++ and Java modules
  * install the platform to $HOME/powsybl


#### Install.sh script options

```
$>./install.sh
usage: ./install.sh [options] [target...]
```

#### Global options

| Option | Description | Default value |
| ------ | ----------- | ------------- |
| --help | Display the install help | |
| --prefix | Set the installation directory | $HOME/powsybl |
| --package-type | Set the package format. The supported formats are zip, tar, tar.gz and tar.bz2 | zip |

#### Third-parties

| Option | Description | Default value |
| ------ | ----------- | ------------- |
| --with-thirdparty | Enable the compilation of third party libraries | |
| --without-thirdparty | Disable the compilation of third party libraries | |
| --thirdparty-prefix | Set the third party installation directory | $HOME/powsybl_thirdparty |
| --thirdparty-download | Sets false to compile third party libraries from a local repository | true |
| --thirdparty-packs | Sets the third party libraries local repository | $HOME/powsybl_packs |

####  Targets  
| Target | Description |
| ------ | ----------- |
| clean | remove previously compiled project's modules |
| clean-thirdparty | Clean the thirdparty libraries |
| compile | Compile project's modules |
| package | Compile project's modules and create a distributable package |
| __install__ | __Compile project's modules and install them__ |
| docs | Generate the documentation (Doxygen/Javadoc) |
| help |  Display the install help |


#### Install configuration file
The install toolchain options, either the default values or those specified at the command line, are saved in the `powsybl-core\install.cfg` configuration file. This configuration file is loaded and updated
each time you use the toolchain.  
*install.cfg* can also be edited using a text editor.


At the end  of installation process, the log will list the installed packages, together with the result of each installation and time spent. For instance:

    Reactor Summary:
    powsybl 1.2.0-SNAPSHOT ............................. SUCCESS [  3.789 s]
    Commons ............................................ SUCCESS [ 29.523 s]
    Math ............................................... SUCCESS [ 16.762 s]
    AFS ................................................ SUCCESS [  0.362 s]
    AFS storage API .................................... SUCCESS [ 10.300 s]
    Computation API .................................... SUCCESS [  7.258 s]
    Tools .............................................. SUCCESS [  6.545 s]
    Local computation .................................. SUCCESS [  4.372 s]
    AFS MapDB storage implementation ................... SUCCESS [ 10.620 s]
    IIDM ............................................... SUCCESS [  0.200 s]
    IIDM network model ................................. SUCCESS [  9.838 s]
    AFS core classes ................................... SUCCESS [ 11.774 s]
    IIDM testing networks .............................. SUCCESS [  1.966 s]
    IIDM network model implementation .................. SUCCESS [ 21.331 s]
    Contingency API .................................... SUCCESS [  8.577 s]
    Action ............................................. SUCCESS [  0.225 s]
    Action DSL SPI ..................................... SUCCESS [  0.733 s]
    IIDM converter API ................................. SUCCESS [  7.894 s]
    IIDM XML converter ................................. SUCCESS [ 13.631 s]
    AFS base extensions ................................ SUCCESS [ 10.622 s]
    Scripting .......................................... SUCCESS [ 10.890 s]
    Load-flow .......................................... SUCCESS [  0.255 s]
    Load-flow API ...................................... SUCCESS [  8.885 s]
    Action utilities ................................... SUCCESS [  4.533 s]
    Action, contingency and rule DSL ................... SUCCESS [ 23.505 s]
    Security analysis .................................. SUCCESS [  0.182 s]
    Security analysis API .............................. SUCCESS [ 11.030 s]
    Action simulator ................................... SUCCESS [ 15.715 s]
    AFS local filesystem implementation ................ SUCCESS [  5.471 s]
    AFS MapDB filesystem implementation ................ SUCCESS [  5.518 s]
    AFS WS ............................................. SUCCESS [  0.165 s]
    AFS WS client side utilities ....................... SUCCESS [  4.044 s]
    AFS WS Utilities ................................... SUCCESS [  2.824 s]
    Network cache AFS .................................. SUCCESS [  0.152 s]
    Network cache AFS client ........................... SUCCESS [  1.835 s]
    AFS WS server side utilities ....................... SUCCESS [  2.495 s]
    Network cache AFS server ........................... SUCCESS [  1.197 s]
    AFS WS storage ..................................... SUCCESS [  3.534 s]
    AFS WS client ...................................... SUCCESS [  2.982 s]
    AFS WS Server ...................................... SUCCESS [02:12 min]
    AMPL converter implementation ...................... SUCCESS [  6.456 s]
    CIM anonymizer ..................................... SUCCESS [  5.751 s]
    CIM1 ............................................... SUCCESS [  0.172 s]
    CIM1 network model ................................. SUCCESS [  7.145 s]
    ENTSO-E utilities .................................. SUCCESS [  6.846 s]
    Load-flow validation ............................... SUCCESS [ 10.114 s]
    Loadflow Results Completion ........................ SUCCESS [  3.939 s]
    CIM1 converter implementation ...................... SUCCESS [  6.223 s]
    MPI computation .................................... SUCCESS [  9.192 s]
    IIDM utilities ..................................... SUCCESS [  3.170 s]
    Security analysis AFS .............................. SUCCESS [  6.284 s]
    Security analysis local service .................... SUCCESS [  5.943 s]
    Time domain simulation API ......................... SUCCESS [  5.885 s]
    UCTE ............................................... SUCCESS [  0.152 s]
    UCTE network model ................................. SUCCESS [  5.894 s]
    UCTE converter implementation ...................... SUCCESS [  4.028 s]
    Distribution 1.2.0-SNAPSHOT ........................ SUCCESS [ 17.275 s]
    BUILD SUCCESS
    Total time: 08:41 min
    Finished at: 2018-07-02T15:51:15Z
                


### Validate the installation
To validate the powsybl-core installation, start a terminal and navigate to the powsybl-core installation ./bin directory.
Here the installation directory and the sources directories are supposed to be, respectively, the default $HOME/powsybl, and $HOME/powsybl-core;
please change the parameters in the command below to reflect your development/installation scenario.

#### convert UCTE network file to XIIDM xml file
This command should convert an UCTE network model, included in the project sources, to XIIDM format
```
$> cd $HOME/powsybl/bin
$> ./itools convert-network --input-file $HOME/powsybl-core/ucte/ucte-network/src/test/resources/20170322_1844_SN3_FR2.uct  --output-format XIIDM --output-file /tmp/test.xiidm 
```
 
