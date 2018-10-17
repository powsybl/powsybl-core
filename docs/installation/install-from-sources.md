# Install Powsybl-Core from the project sources

## "How to run Powsybl by building sources" scenario
The instructions below explain how to build powsybl-core sources into a full powsybl binary installation.

### Environment requirements
Linux is required, this guide covers these distributions
 * CentOS v6.x / v7.x

In order to build the project, you need:
  * Java Development Kit (JDK): *(Java 8 or newer)*
  * [Maven](https://maven.apache.org) (>= 3.5.3)
  * CMake *(>= 2.6)*
  * A recent C++ compiler (GNU g++ or Clang)
  * [OpenMPI](https://www.open-mpi.org/) *(tested with 1.8.3)*
  * Some development libraries (e.g. [zlib](http://www.zlib.net/) and [bzip](http://www.bzip.org/))

You might also need to configure your network proxy settings, as the installation procedure downloads files from external repositories, e.g. GitHub and Maven central.

### Build tools and libraries installation
Root privileges are required to install these packages (used to build some external modules, e.g. Boost):
```
$> sudo yum group install "Development Tools"
$> sudo yum install cmake icu tar wget bzip2-devel zlib-devel
```
Note that `sudo yum group install "Development Tools"` installs a set of predefined CentOS development tools, including gcc, g++, git.

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
$> cd powsybl-core
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
  * package a Powsybl platform distribution (using the [itools-packager](itools-packager/README.md) maven plugin)
  * install the platform distribution to $HOME/powsybl


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

```
  [INFO] Reactor Summary:
  [INFO]
  [INFO] powsybl 2.2.0-SNAPSHOT ............................. SUCCESS [  4.279 s]
  [INFO] Commons ............................................ SUCCESS [ 13.805 s]
  [INFO] Time series ........................................ SUCCESS [  0.409 s]
  [INFO] Time series API .................................... SUCCESS [  4.648 s]
  [INFO] AFS ................................................ SUCCESS [  0.394 s]
  [INFO] AFS storage API .................................... SUCCESS [  4.214 s]
  [INFO] Computation API .................................... SUCCESS [  2.892 s]
  [INFO] Tools .............................................. SUCCESS [  3.756 s]
  [INFO] Local computation .................................. SUCCESS [  2.866 s]
  [INFO] AFS MapDB storage implementation ................... SUCCESS [  4.853 s]
  [INFO] IIDM ............................................... SUCCESS [  0.412 s]
  [INFO] IIDM network model ................................. SUCCESS [  5.701 s]
  [INFO] AFS core classes ................................... SUCCESS [  6.028 s]
  [INFO] Math ............................................... SUCCESS [  2.746 s]
  [INFO] IIDM testing networks .............................. SUCCESS [  0.717 s]
  [INFO] IIDM network model implementation .................. SUCCESS [  7.494 s]
  [INFO] Contingency API .................................... SUCCESS [  5.241 s]
  [INFO] Action ............................................. SUCCESS [  0.313 s]
  [INFO] Action DSL SPI ..................................... SUCCESS [  0.503 s]
  [INFO] IIDM converter API ................................. SUCCESS [  4.112 s]
  [INFO] IIDM XML converter ................................. SUCCESS [  7.116 s]
  [INFO] AFS base extensions ................................ SUCCESS [  4.651 s]
  [INFO] Scripting .......................................... SUCCESS [  5.429 s]
  [INFO] Load-flow .......................................... SUCCESS [  0.302 s]
  [INFO] Load-flow API ...................................... SUCCESS [  4.291 s]
  [INFO] Action utilities ................................... SUCCESS [  2.864 s]
  [INFO] Action, contingency and rule DSL ................... SUCCESS [ 10.381 s]
  [INFO] Security analysis .................................. SUCCESS [  0.304 s]
  [INFO] Security analysis API .............................. SUCCESS [  4.329 s]
  [INFO] Action simulator ................................... SUCCESS [  7.290 s]
  [INFO] AFS local filesystem implementation ................ SUCCESS [  2.531 s]
  [INFO] AFS MapDB filesystem implementation ................ SUCCESS [  2.944 s]
  [INFO] AFS WS ............................................. SUCCESS [  0.292 s]
  [INFO] AFS WS Utilities ................................... SUCCESS [  1.593 s]
  [INFO] AFS WS client side utilities ....................... SUCCESS [  2.216 s]
  [INFO] Network cache AFS .................................. SUCCESS [  0.355 s]
  [INFO] Network cache AFS client ........................... SUCCESS [  0.711 s]
  [INFO] AFS WS server side utilities ....................... SUCCESS [  1.752 s]
  [INFO] Network cache AFS server ........................... SUCCESS [  0.617 s]
  [INFO] AFS WS storage ..................................... SUCCESS [  2.159 s]
  [INFO] AFS WS client ...................................... SUCCESS [  1.896 s]
  [INFO] AFS WS Server ...................................... SUCCESS [01:04 min]
  [INFO] AMPL converter implementation ...................... SUCCESS [  3.407 s]
  [INFO] CIM anonymizer ..................................... SUCCESS [  5.271 s]
  [INFO] CIM1 ............................................... SUCCESS [  0.328 s]
  [INFO] CIM1 network model ................................. SUCCESS [  4.747 s]
  [INFO] ENTSO-E utilities .................................. SUCCESS [  5.038 s]
  [INFO] Load-flow validation ............................... SUCCESS [  7.706 s]
  [INFO] Loadflow Results Completion ........................ SUCCESS [  2.909 s]
  [INFO] CIM1 converter implementation ...................... SUCCESS [  3.759 s]
  [INFO] MPI computation .................................... SUCCESS [  3.997 s]
  [INFO] IIDM utilities ..................................... SUCCESS [  1.993 s]
  [INFO] Security analysis AFS .............................. SUCCESS [  3.303 s]
  [INFO] Security analysis local service .................... SUCCESS [  6.436 s]
  [INFO] Sensitivity computation API ........................ SUCCESS [  3.475 s]
  [INFO] Time domain simulation API ......................... SUCCESS [  3.188 s]
  [INFO] UCTE ............................................... SUCCESS [  0.329 s]
  [INFO] UCTE network model ................................. SUCCESS [  2.518 s]
  [INFO] UCTE converter implementation ...................... SUCCESS [  2.215 s]
  [INFO] iTools packager Maven plugin ....................... SUCCESS [  2.055 s]
  [INFO] Distribution 2.2.0-SNAPSHOT ........................ SUCCESS [  3.806 s]
  [INFO] ------------------------------------------------------------------------
  [INFO] BUILD SUCCESS
  [INFO] ------------------------------------------------------------------------
  [INFO] Total time: 04:32 min
  [INFO] Finished at: 2018-10-02T18:51:21+02:00
  [INFO] ------------------------------------------------------------------------
```

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

## Note
If you want to learn how to build a binary distribution from maven central, please refer to the [installation guide](install-from-maven.md).