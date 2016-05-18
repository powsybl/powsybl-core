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

if [ -z "$MATLABHOME" ]; then
    echo "Need to set enviroment variable MATLABHOME"
    exit 1
fi

export PATH=$MATLABHOME/bin:$PATH

cwd=$(pwd)
targetfolder="$cwd/bin"
mkdir "$targetfolder"

cd sources

exename=wp43_smallsignal
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
 smallsignal_HELPER.m \
 Dynamic_smallsignal_example.m \
 pronyiTesla.m \
 signal_filter.m \
 sssi.m

cd $cwd 
