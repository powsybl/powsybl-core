#!/bin/bash

# Copyright (c) 2016, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

##
## before setting these settings to true, make sure that these enviroment variables are available 
## - EUROSTAG_SRC_HOME and  INTEL_HOME (to compile EUROSTAG based modules)
## - MATLABHOME: to compile MATLAB CODE, must point to the root of a MATLAB v>R2015 installation
## - DYMOLAHOME: to compile the matlab integration module)
##
BUILD_EUROSTAG=false
BUILD_MATLAB=false
BUILD_DYMOLA=false

## default ipst installation target directory
installDir=$HOME/itesla
## default ipst thirdparty directory
thirdpartyDir=$HOME/itesla_thirdparty

## stop script execution if installDir already exists
wipeInstallDir=false

cmd=$0
usage() {
    echo "usage: $cmd [--help] [--installDir <installation path>] [--thirdpartyDir <thirdparty path>] [--wipeInstallDir] [--buildMATLAB] [--buildEUROSTAG] [--buildDYMOLA]";
    echo ""
    exit
}

help() {
    echo "usage: $cmd [--help] [--installDir <installation path>] [--thirdpartyDir <thirdparty path>] [--wipeInstallDir]";
    echo "   --installDir       the target directory; default is <HOME>/itesla;  installation will not proceed if it already exists,";
    echo "                      unless --wipeInstallDir is set (in this case the existing path will be removed)";
    echo "   --thirdpartyDir    the target path for the thirdparty libraries, required to build IPST (default is <HOME>/itesla_thirdparty)";
    echo "   --wipeInstallDir   if set, installDir will be deleted (default is true)";
    echo "   --buildMATLAB      if set, build MATLAB components (default is false, this option requires installation of MATLAB and MATLAB compiler)";
    echo "   --buildEUROSTAG    if set, build EUROSTAG components (default is false, this option requires installation of EUROSTAG and EUROSTAG SDK)";
    echo "   --buildDYMOLA      if set, build DYMOLA components (default is false, this option requires installation of DYMOLA)";
    echo "   --help  ";
    echo ""
    exit
}


for ((i=1;i<=$#;i++)); 
do
    if [ ${!i} = "--installDir" ] 
    then ((i++)) 
        installDir=${!i};
    elif [ ${!i} = "--thirdpartyDir" ];
    then ((i++)) 
        thirdpartyDir=${!i};  
    elif [ ${!i} = "--wipeInstallDir" ];
    then 
        wipeInstallDir=true;  
    elif [ ${!i} = "--buildMATLAB" ];
    then 
        BUILD_MATLAB=true;  
    elif [ ${!i} = "--buildEUROSTAG" ];
    then 
        BUILD_EUROSTAG=true;  
    elif [ ${!i} = "--buildDYMOLA" ];
    then 
        BUILD_DYMOLA=true;  
    elif [ ${!i} = "--help" ];    
    then ((i++)) 
        help;
    fi
done;

echo "installDir:" $installDir
echo "thirdpartyDir:" $thirdpartyDir
echo "wipeInstallDir:" $wipeInstallDir

if [ -d $installDir ] &&
   [ $wipeInstallDir != true ]
then
    echo "ERROR: the target installation directory '"$installDir"' already exists"
    echo ""
    usage;
fi


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

