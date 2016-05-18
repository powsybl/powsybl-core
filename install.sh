#!/bin/bash

# Copyright (c) 2016, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.


## ipst installation target directory
installDir=$HOME/itesla
## ipst thirdparty
thirdpartyDir=$HOME/itesla_thirdparty


##
## before setting these settings to true, make sure that these enviroment variables are available 
## - EUROSTAG_SRC_HOME and  INTEL_HOME (to compile EUROSTAG based modules)
## - MATLABHOME: to compile MATLAB CODE, must point to the root of a MATLAB v>R2015 installation
## - DYMOLAHOME: to compile the matlab integration module)
##
BUILD_EUROSTAG=false
BUILD_MATLAB=false
BUILD_DYMOLA=false

###############################
# remove  previous installation
rm -rf $installDir
mkdir -p $installDir

############################################
# build required C/C++ thirdparty libraries:
# - boost, build, hdf5, libarchive, log4cpp, matio, protobuf, szip, zlib
#
buildThirdpartyDir=$thirdpartyDir/build
cmake -Dthirdparty_prefix=$thirdpartyDir -G "Unix Makefiles" -Hthirdparty -B$buildThirdpartyDir 
make -C $buildThirdpartyDir

###########################################################################################
# build IPST (C/C++ and MATLAB modules, if enabled by the above declared BUILD_MATLAB flag)
#
buildDir=./build
cmake -DCMAKE_INSTALL_PREFIX=$installDir -Dthirdparty_prefix=$thirdpartyDir -DBUILD_EUROSTAG=$BUILD_EUROSTAG -DBUILD_MATLAB=$BUILD_MATLAB -DBUILD_DYMOLA=$BUILD_DYMOLA  -G "Unix Makefiles" -H. -B$buildDir 
make -C $buildDir

###########################
# build IPST (Java modules)
#
mvn -f ./pom.xml clean install

################################################
#install IPST to the target directory: installDir
#
cp -r ./distribution/target/distribution-*-full/itesla/* $installDir/

################################
# create a default configuration
#
mkdir $installDir/etc
echo "#itesla_cache_dir=" >> $installDir/etc/itesla.conf
echo "#itesla_config_dir=" >> $installDir/etc/itesla.conf
echo "itesla_config_name=config" >> $installDir/etc/itesla.conf
echo "mpi_tasks=3" >> $installDir/etc/itesla.conf
echo "mpi_hosts=localhost" >> $installDir/etc/itesla.conf

