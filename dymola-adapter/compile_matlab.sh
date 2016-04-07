#!/bin/bash
#
# Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
#

###############################
#compile wp43 V1.3 matlab modules
###############################

#export MATLABHOME=/adisk03/MATLAB/MATLAB_Production_Server/R2013a/bin
#export DYMOLAHOME


if [ -z "$MATLABHOME" ]; then
    echo "Need to set enviroment variable MATLABHOME"
    exit 1
fi

if [ -z "$DYMOLAHOME" ]; then
    echo "Need to set enviroment variable DYMOLAHOME, pointing to a Dymola installation. Compilation requires two files from DYMOLAHOME/Mfiles/dymtools: dymload.m and dymget.m"
    exit 1
fi


export PATH=$MATLABHOME/bin:$PATH

cwd=$(pwd)
targetfolder="$cwd/bin"
mkdir "$targetfolder"

cd sources

exename=wp43dymadapter_overload
mcc -o $exename \
 -W main:$exename \
 -T link:exe \
 -d "$targetfolder" \
 -N \
 -p stats \
 -w enable:specified_file_mismatch \
 -w enable:repeated_file \
 -w enable:switch_ignored \
 -w enable:missing_lib_sentinel \
 -w enable:demo_license \
 -R -nojvm \
 -R -nodisplay \
 -R -singleCompThread \
 -R -nosplash \
 -v \
 wp43dymadapter_overload.m \
 $DYMOLAHOME/Mfiles/dymtools/dymload.m \
 $DYMOLAHOME/Mfiles/dymtools/dymget.m

exename=wp43dymadapter_smallsignal
mcc -o $exename \
 -W main:$exename \
 -T link:exe \
 -d "$targetfolder" \
 -N \
 -p stats \
 -w enable:specified_file_mismatch \
 -w enable:repeated_file \
 -w enable:switch_ignored \
 -w enable:missing_lib_sentinel \
 -w enable:demo_license \
 -R -nojvm \
 -R -nodisplay \
 -R -singleCompThread \
 -R -nosplash \
 -v \
 wp43dymadapter_smallsignal.m \
 $DYMOLAHOME/Mfiles/dymtools/dymload.m \
 $DYMOLAHOME/Mfiles/dymtools/dymget.m

exename=wp43dymadapter_transient
mcc -o $exename \
 -W main:$exename \
 -T link:exe \
 -d "$targetfolder" \
 -N \
 -p stats \
 -w enable:specified_file_mismatch \
 -w enable:repeated_file \
 -w enable:switch_ignored \
 -w enable:missing_lib_sentinel \
 -w enable:demo_license \
 -R -nojvm \
 -R -nodisplay \
 -R -singleCompThread \
 -R -nosplash \
 -v \
 wp43dymadapter_transient.m \
 $DYMOLAHOME/Mfiles/dymtools/dymload.m \
 $DYMOLAHOME/Mfiles/dymtools/dymget.m


exename=wp43dymadapter_underovervoltage
mcc -o $exename \
 -W main:$exename \
 -T link:exe \
 -d "$targetfolder" \
 -N \
 -p stats \
 -w enable:specified_file_mismatch \
 -w enable:repeated_file \
 -w enable:switch_ignored \
 -w enable:missing_lib_sentinel \
 -w enable:demo_license \
 -R -nojvm \
 -R -nodisplay \
 -R -singleCompThread \
 -R -nosplash \
 -v \
 wp43dymadapter_underovervoltage.m \
 $DYMOLAHOME/Mfiles/dymtools/dymload.m \
 $DYMOLAHOME/Mfiles/dymtools/dymget.m

cp wp43_dymola.sh $targetfolder

cd $cwd 
