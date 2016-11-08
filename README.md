[![Build Status](https://travis-ci.org/itesla/ipst.svg?branch=master)](https://travis-ci.org/itesla/ipst)
[![Coverage Status](https://coveralls.io/repos/github/itesla/ipst/badge.svg?branch=master)](https://coveralls.io/github/itesla/ipst?branch=master)
[![MPL-2.0 License](https://img.shields.io/badge/license-MPL_2.0-blue.svg)](https://www.mozilla.org/en-US/MPL/2.0/)

# iTESLA
http://www.itesla-project.eu/

http://www.itesla-pst.org

## Environment requirements
In order to build iPST you need:
  * JDK *(1.8 or greater)*
  * Maven 
  * CMake *(2.6 or greater)*
  * Recent C++ compiler (GNU g++ or Clang)
  * OpenMPI *(1.8.3 or greater)*
  
Optional C++ modules requires:
  * Intel Fortran Compiler 
  * Matlab
  * Dymola
  * Eurostag

### OpenMPI (required)
In order to support the MPI modules, you need to compile and install the [OpenMPI](https://www.open-mpi.org/) library.
```
$> wget http://www.open-mpi.org/software/ompi/v1.8/downloads/openmpi-1.8.3.tar.bz2
$> tar xjf openmpi-1.8.3.tar.bz2
$> cd openmpi-1.8.3
$> ./configure --prefix=<INSTALL_DIR> --enable-mpi-thread-multiple
$> make install
$> export PATH=$PATH:<INSTALL_DIR>/bin
```

### Intel Fortran Compiler
To compile Eurostag dependant modules, you have to set your INTEL_HOME environment variable to the root of the
Intel Fortran Compiler installation.

## Install
To easily compile iPST, you can use the toolchain:
```
$> git clone https://github.com/itesla/ipst.git
$> ./install.sh
```
By default, the toolchain will:
  * download and compile all external packages from the Internet
  * compile C++ and Java modules
  * install iPST

### Targets

| Target | Description |
| ------ | ----------- |
| clean | Clean iPST modules |
| clean-thirdparty | Clean the thirdparty libraries |
| compile | Compile iPST modules |
| package | Compile iPST modules and create a distributable package |
| __install__ | __Compile iPST modules and install it__ |
| docs | Generate the documentation (Doxygen/Javadoc) |
| help | Display this help |

### Options

The toolchain options are saved in the *install.cfg* configuration file. This configuration file is loaded and updated
each time you use the toolchain.

#### iPST

| Option | Description | Default value |
| ------ | ----------- | ------------- |
| --help | Display this help | |
| --prefix | Set the installation directory | $HOME/itesla |
| --package-type | Set the package format. The supported formats are zip, tar, tar.gz and tar.bz2 | zip |

#### C++ modules

| Option | Description | Default value |
| ------ | ----------- | ------------- |
| --with-dymola | Enable the compilation of Dymola dependant modules | $DYMOLA_HOME |
| --without-dymola | Disable the compilation of Dymola dependant modules | |
| --with-eurostag | Enable the compilation of Eurostag dependant modules | $EUROSTAG_SDK_HOME |
| --without-eurostag | Disable the compilation of Eurostag dependant modules | |
| --with-matlab | Enable the compilation of Matlab dependant modules | $MATLAB_HOME |
| --without-matlab | Disable the compilation of Matlab dependant modules | |

#### Third-parties

| Option | Description | Default value |
| ------ | ----------- | ------------- |
| --with-thirdparty | Enable the compilation of thirdparty libraries | |
| --without-thirdparty | Disable the compilation of thirdparty libraries | |
| --thirdparty-prefix | Set the thirdparty installation directory | $HOME/itesla_thirdparty |
| --thirdparty-download | Sets false to compile thirdparty libraries from a local repository | true |
| --thirdparty-packs | Sets the thirdparty libraries local repository | $HOME/itesla_packs |

### Default configuration file
```
ipst_prefix=$HOME/itesla
ipst_package_type=zip

#  -- iPST C++ modules options --
eurostag_build=false
eurostag_home=$EUROSTAG_SDK_HOME
dymola_build=false
dymola_home=$DYMOLA_HOME
matlab_build=false
matlab_home=$MATLAB_HOME

#  -- iPST thirdparty libraries --
thirdparty_build=true
thirdparty_prefix=$HOME/itesla_thirdparty
thirdparty_download=true
thirdparty_packs=$HOME/itesla_packs
```

## License
https://www.mozilla.org/en-US/MPL/2.0/
