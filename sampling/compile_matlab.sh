#!/bin/bash
#
# Copyright (c) 2016, Quinary <itesla@quinary.com>
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
#

###############################
#compile wp41 V6.7 matlab modules
###############################

if [ -z "$MATLABHOME" ]; then
    echo "Need to set enviroment variable MATLABHOME"
    exit 1
fi

export PATH=$MATLABHOME/bin:$PATH

cwd=$(pwd)
targetfolder="$cwd/bin"
mkdir "$targetfolder"

#module1
exename=wp41c_v67_m1
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
  sources/MODULE1_HELPERS.m \
 -a sources/MODULE1_HELPER.m \
 -a sources/module1.m 


#module2
exename=wp41c_v67_m2
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
  sources/MODULE2_HELPERS.m \
 -a sources/MODULE2_HELPER.m \
 -a sources/module2.m \
 -a sources/h.m \
 -a sources/NEW_PIT.m \
 -a sources/copulachoose.m \
 -a sources/map_ecdf.m \
 -a sources/copulaName.m \
 -a sources/modU.m 


#module3pre
exename=wp41c_v67_m3pre
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
  sources/MODULE3PRE_HELPERS.m


#module3
exename=wp41c_v67_m3
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
  sources/MODULE3_HELPERS.m \
 -a sources/MODULE3_HELPER.m \
 -a sources/module3.m \
 -a sources/h_inv_ex.m \
 -a sources/modU.m \
 -a sources/sQuantile.m 


#module3 aggregator
exename=wp41c_v67_aggregator
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
  sources/AGGREGATORS.m \
 -a sources/AGGREGATOR.m \
 -a sources/insert_ind.m 
