#!/bin/bash
#
# Copyright (c) 2016, Quinary <itesla@quinary.com>
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
#

#############################################
#compile wp5 matlab mcla V1.8
#############################################

#export MATLABHOME=/adisk03/MATLAB/MATLAB_Production_Server/R2013a/bin
#export MATLABHOME=/home/matlab/R2014a/bin

if [ -z "$MATLABHOME" ]; then
    echo "Need to set enviroment variable MATLABHOME"
    exit 1
fi

export PATH=$MATLABHOME/bin:$PATH

cwd=$(pwd)
targetfolder="$cwd/bin"
mkdir "$targetfolder"

cd sources

exename=wp5mcla
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
 -R -nosplash \
 -v \
 MCLA_HELPER.m \
-a analysis.m \
-a bisection.m \
-a carica_DB.m \
-a check_distrizuione.m \
-a chi_pval.m \
-a closest_corr.m \
-a conditional_samps.m \
-a copulaName.m \
-a copulachoose.m \
-a datacorrige2.m \
-a estract_cdf.m \
-a funzioneB.m \
-a funzione.m \
-a gausmix3.m \
-a gaussian_conditional.m \
-a gaussian_mixture.m \
-a h_gumbel.m \
-a h_inv_ex.m \
-a h_inv.m \
-a h.m \
-a h_pit.m \
-a inversion_with_verify2.m \
-a invfunzioneB.m \
-a licols.m \
-a main.m \
-a main_MCLA2PC3.m \
-a map_ecdf.m \
-a mod_data.m \
-a modelSummary.m \
-a modR.m \
-a MODULE0.m \
-a MODULE1_HELPER.m \
-a MODULE1_HELPERS.m \
-a MODULE1.m \
-a MODULE2_HELPER.m \
-a MODULE2_HELPERS.m \
-a MODULE2.m \
-a MODULE2_OUTPUT2.m \
-a MODULE2_OUTPUT.m \
-a MODULE3_HELPER.m \
-a MODULE3_HELPERS.m \
-a MODULE3.m \
-a MODULE3PRE_HELPERS.m \
-a MODULE3_SINGLE.m \
-a modU.m \
-a new_method_imputation4.m \
-a new_method_imputation.m \
-a NEW_PIT.m \
-a pictures.m \
-a rand_gen.m \
-a run_module3.m \
-a sQuantile.m \
-a stimaposdef.m \
-a testfilterings2.m \
-a trova_closest.m


exename=wp5fpf
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
 -R -nosplash \
 -v \
 FPF_HELPER.m \
-a analysis.m \
-a bisection.m \
-a carica_DB.m \
-a check_distrizuione.m \
-a chi_pval.m \
-a closest_corr.m \
-a conditional_samps.m \
-a copulaName.m \
-a copulachoose.m \
-a datacorrige2.m \
-a estract_cdf.m \
-a funzioneB.m \
-a funzione.m \
-a gausmix3.m \
-a gaussian_conditional.m \
-a gaussian_mixture.m \
-a h_gumbel.m \
-a h_inv_ex.m \
-a h_inv.m \
-a h.m \
-a h_pit.m \
-a inversion_with_verify2.m \
-a invfunzioneB.m \
-a licols.m \
-a main.m \
-a main_MCLA2PC3.m \
-a map_ecdf.m \
-a mod_data.m \
-a modelSummary.m \
-a modR.m \
-a MODULE0.m \
-a MODULE1_HELPER.m \
-a MODULE1_HELPERS.m \
-a MODULE1.m \
-a MODULE2_HELPER.m \
-a MODULE2_HELPERS.m \
-a MODULE2.m \
-a MODULE2_OUTPUT2.m \
-a MODULE2_OUTPUT.m \
-a MODULE3_HELPER.m \
-a MODULE3_HELPERS.m \
-a MODULE3.m \
-a MODULE3PRE_HELPERS.m \
-a MODULE3_SINGLE.m \
-a modU.m \
-a new_method_imputation4.m \
-a new_method_imputation.m \
-a NEW_PIT.m \
-a pictures.m \
-a rand_gen.m \
-a run_module3.m \
-a sQuantile.m \
-a stimaposdef.m \
-a testfilterings2.m \
-a trova_closest.m

cd $cwd 
