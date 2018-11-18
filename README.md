# Powsybl - core

[![Build Status](https://travis-ci.org/powsybl/powsybl-core.svg?branch=master)](https://travis-ci.org/powsybl/powsybl-core)
[![Build status](https://ci.appveyor.com/api/projects/status/76o2bbmsewpbpr97/branch/master?svg=true)](https://ci.appveyor.com/project/powsybl/powsybl-core/branch/master)
[![Coverage Status](https://coveralls.io/repos/github/powsybl/powsybl-core/badge.svg?branch=master)](https://coveralls.io/github/powsybl/powsybl-core?branch=master)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=com.powsybl%3Apowsybl-core&metric=coverage)](https://sonarcloud.io/component_measures?id=com.powsybl%3Apowsybl-core&metric=coverage)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=com.powsybl%3Apowsybl-core&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.powsybl%3Apowsybl-core)
[![MPL-2.0 License](https://img.shields.io/badge/license-MPL_2.0-blue.svg)](https://www.mozilla.org/en-US/MPL/2.0/)

http://www.powsybl.com

## Environment requirements
In order to build the project, you need:
  * JDK *(1.8 or greater)*
  * Maven *(3.3.9 or greater)*
  * CMake *(2.6 or greater)*
  * Recent C++ compiler (GNU g++ or Clang)
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
In order to build Boost external package, you have to install [zlib](http://www.zlib.net/) library.
```
$> yum install zlib-devel
```

### bzip2 (required)
In order to build Boost external package, you have to install [bzip](http://www.bzip.org/) library.
```
$> yum install bzip2-devel
```

## Install
To easily compile, you can use the toolchain:
```
$> git clone https://github.com/powsybl/powsybl-core.git
$> ./install.sh
```
By default, the toolchain will:
  * download and compile all external packages from the Internet
  * compile C++ and Java modules
  * install the platform

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

The toolchain options are saved in the *install.cfg* configuration file. This configuration file is loaded and updated
each time you use the toolchain.

#### Global options

| Option | Description | Default value |
| ------ | ----------- | ------------- |
| --help | Display this help | |
| --prefix | Set the installation directory | $HOME/powsybl |
| --package-type | Set the package format. The supported formats are zip, tar, tar.gz and tar.bz2 | zip |

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
powsybl_package_type=zip

#  -- Thirdparty libraries --
thirdparty_build=true
thirdparty_prefix=$HOME/powsybl_thirdparty
thirdparty_download=true
thirdparty_packs=$HOME/powsybl_packs
```
