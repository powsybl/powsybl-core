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
BUILD_THIRDPARTY=true

## ipst top source directory
sourceDir=$(dirname $(readlink -f $0))

## default ipst installation target directory
installDir=$HOME/itesla
## default ipst thirdparty directory
thirdpartyDir=$HOME/itesla_thirdparty

## stop script execution if installDir already exists
overwriteInstallation=false

## remove non java ipst build dir, before triggering a new build
removeNonJavaBuildDir=false

## remove thord-party build dir, before triggering a new build
removeThirdpartyBuildDir=false


cmd=$0
usage() {
    echo "usage: $cmd [--help] [--installDir <installation path>] [--thirdpartyDir <thirdparty path>] [--overwriteInstallation] [--clean] [--removeNonJavaBuildDir] [--removeThirdpartyBuildDir] [--buildMATLAB] [--buildEUROSTAG] [--buildDYMOLA]";
    echo ""
    exit 1
}

help() {
    echo "usage: $cmd [--help] [--installDir <installation path>] [--thirdpartyDir <thirdparty path>] [--overwriteInstallation]";
    echo "   --installDir               the target directory; default is <HOME>/itesla;  installation will not proceed if it already exists,";
    echo "                              unless --overwriteInstallation is set (in this case the existing installation will be overwritten)";
    echo "   --thirdpartyDir            the target path for the thirdparty libraries, required to build IPST (default is <HOME>/itesla_thirdparty)";
    echo "   --overwriteInstallation    if set, the install script overwrites an existing installation (default is not set: stop script execution if installDir already exists)";
    echo "   --buildMATLAB              if set, build MATLAB components (default is false, this option requires installation of MATLAB and MATLAB compiler)";
    echo "   --buildEUROSTAG            if set, build EUROSTAG components (default is false, this option requires installation of EUROSTAG and EUROSTAG SDK)";
    echo "   --buildDYMOLA              if set, build DYMOLA components (default is false, this option requires installation of DYMOLA)";
    echo "   --removeThirdpartyBuildDir if set, remove an already existing third-party build dir before starting a new build (default is false)";
    echo "   --removeNonJavaBuildDir    if set, remove an already existing non java ipst  build dir before starting a new build (default is false)";
    echo "   --clean                    remove compiled files and directories generated during a build (thirdpartyDir excluded) and exit.";
    echo "   --help  ";
    echo ""
    exit
}


cleanIPST=false;

for ((i=1;i<=$#;i++));
do
    if [ ${!i} = "--installDir" ] 
    then ((i++)) 
        installDir=${!i};
    elif [ ${!i} = "--thirdpartyDir" ];
    then ((i++)) 
        thirdpartyDir=${!i};  
    elif [ ${!i} = "--overwriteInstallation" ];
    then 
        overwriteInstallation=true;  
    elif [ ${!i} = "--buildMATLAB" ];
    then 
        BUILD_MATLAB=true;  
    elif [ ${!i} = "--buildEUROSTAG" ];
    then 
        BUILD_EUROSTAG=true;  
    elif [ ${!i} = "--buildDYMOLA" ];
    then 
        BUILD_DYMOLA=true;  
    elif [ ${!i} = "--skipThirdpartyBuild" ];
    then 
        BUILD_THIRDPARTY=false;  
    elif [ ${!i} = "--removeThirdpartyBuildDir" ];
    then 
        removeThirdpartyBuildDir=true;  
    elif [ ${!i} = "--removeNonJavaBuildDir" ];
    then 
        removeNonJavaBuildDir=true;  
    elif [ ${!i} = "--clean" ];
    then 
        cleanIPST=true;  
    elif [ ${!i} = "--help" ];    
    then ((i++)) 
        help;
    fi
done;

buildDir=$sourceDir/build
buildThirdpartyDir=$thirdpartyDir/build

if [ $cleanIPST = true ] ; then
    echo ""
    echo "** cleaning ipst"
    echo ""
    mvn -f $sourceDir/pom.xml clean
    cr=$?
    if [ $cr -ne 0 ] ; then
        exit $cr
    fi
    rm -rf $buildDir

    echo ""
    echo "** cleaned."
    echo ""

    exit 0
fi

echo ""
echo "** Building and installing ipst:"
echo "** -----------------------------"
echo "** installDir:" $installDir
echo "** thirdpartyDir:" $thirdpartyDir
echo "** overwriteInstallation:" $overwriteInstallation
echo "** removeThirdpartyBuildDir:" $removeThirdpartyBuildDir
echo "** removeNonJavaBuildDir:" $removeNonJavaBuildDir
echo "** buildMATLAB:" $BUILD_MATLAB
echo "** buildEUROSTAG:" $BUILD_EUROSTAG
echo "** buildDYMOLA:" $BUILD_DYMOLA

if [ $BUILD_MATLAB = true ] ; then
 if [ -z "$MATLABHOME" ] ; then
    echo ""
    echo "ERROR: to build MATLAB modules, MATLABHOME environment variable is required; MATLABHOME must point to the root of a licensed MATLAB installation (with MATLAB compiler)"
    exit 1
 fi  
fi

if [ $BUILD_EUROSTAG = true ] ; then
 if [ -z "$EUROSTAG_SRC_HOME" ] ; then
    echo ""
    echo "ERROR: to build EUROSTAG modules, EUROSTAG_SRC_HOME environment variable is required. It must point to an EUROSTAG SDK."
    exit 1
 fi  
fi

if [ $BUILD_DYMOLA = true ] ; then
 if [ -z "$DYMOLAHOME" ] ; then
    echo ""
    echo "ERROR: to build DYMOLA modules, DYMOLAHOME environment variable is required. It must point to the root of a DYMOLA installation."
    exit 1
 fi  
fi

if [ -d $installDir ] &&
   [ $overwriteInstallation != true ]
then
    echo ""
    echo "ERROR: installation cannot continue: the target directory '"$installDir"' already exists (to allow overwrite it, set the --overtriteInstallation parameter)."
    echo ""
    usage;
fi


############################################
# build required C/C++ thirdparty libraries:
# - boost, build, hdf5, libarchive, log4cpp, matio, protobuf, szip, zlib
#
if [ $BUILD_THIRDPARTY = true ] ; then
 echo ""
 echo "** Building thirdparty libraries"
 echo ""
 if [ $removeThirdpartyBuildDir = true  ] ; then
   echo ""
   echo "*** removing third-party build dir (if it already exists). Triggers a clean third-party build."
   echo ""
   rm -rf $buildThirdpartyDir
 fi
 cmake -Dthirdparty_prefix=$thirdpartyDir -G "Unix Makefiles" -H$sourceDir/thirdparty -B$buildThirdpartyDir 
 cr=$?
 if [ $cr -ne 0 ] ; then
  exit $cr
 fi
 make -C $buildThirdpartyDir
 cr=$?
 if [ $cr -ne 0 ] ; then
  exit $cr
 fi
else
 echo ""
 echo "** Exclude required thirdparty from build; libraries are assumed to be already available here: "$thirdpartyDir
 echo ""
fi

###########################################################################################
# build IPST (C/C++ and MATLAB modules, if enabled by the above declared BUILD_MATLAB flag)
#
echo ""
echo "** Building IPST platform: C/C++ and MATLAB (when configured) modules"
echo ""
if [ $removeNonJavaBuildDir = true  ] ; then
   echo ""
   echo "*** removing non java build dir (if it already exists). Triggers a clean build."
   echo ""
   rm -rf $buildDir
fi
cmake -DCMAKE_INSTALL_PREFIX=$installDir -Dthirdparty_prefix=$thirdpartyDir -DBUILD_EUROSTAG=$BUILD_EUROSTAG -DBUILD_MATLAB=$BUILD_MATLAB -DBUILD_DYMOLA=$BUILD_DYMOLA  -G "Unix Makefiles" -H$sourceDir -B$buildDir 
cr=$?
if [ $cr -ne 0 ] ; then
exit $cr
fi
make -C $buildDir
cr=$?
if [ $cr -ne 0 ] ; then
exit $cr
fi


###########################
# build IPST (Java modules)
#
echo ""
echo "** Building IPST platform: java modules"
echo ""
mvn -f $sourceDir/pom.xml clean install
cr=$?
if [ $cr -ne 0 ] ; then
exit $cr
fi

################################################
#install IPST to the target directory: installDir
#
echo ""
echo "** Copying distribution files to "$installDir
echo ""
mkdir -p $installDir
cp -r $sourceDir/distribution/target/distribution-*-full/itesla/* $installDir/
cr=$?
if [ $cr -ne 0 ] ; then
exit $cr
fi

mkdir -p $installDir/etc

######################################################################################
# create a default configuration file, if a configuration file  does not already exist
#
if [ ! -f $installDir/etc/itesla.conf ]; then
echo ""
echo "*** Creating a default itesla.conf file"
echo ""
echo "#itesla_cache_dir=" >> $installDir/etc/itesla.conf
echo "#itesla_config_dir=" >> $installDir/etc/itesla.conf
echo "itesla_config_name=config" >> $installDir/etc/itesla.conf
echo "mpi_tasks=3" >> $installDir/etc/itesla.conf
echo "mpi_hosts=localhost" >> $installDir/etc/itesla.conf
else
echo "*** Configuration file " $installDir/etc/itesla.conf " already exists: it will not be replaced."
fi
echo ""

exit 0