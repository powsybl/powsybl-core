# Copyright (c) 2016, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

cmake_minimum_required (VERSION 2.6)

set(exec_name1 wp5fea_m1)
set(exec_name2 wp5fea_m2)
set(exec_name3 wp5fea_m2_reduce)
set(exec_name4 wp5fea_m3_sampling)
set(exec_name5 wp5mcla)
set(exec_name6 wp5fpf)

set(matlabhome $ENV{MATLABHOME})
set(sdir ${CMAKE_CURRENT_SOURCE_DIR}/sources)
set(builddir ${CMAKE_CURRENT_BINARY_DIR})

add_custom_command(
    COMMENT "Compiling ${exec_name1}"
    OUTPUT  ${exec_name1}
    DEPENDS ${sdir}/*.m 
    COMMAND ${matlabhome}/bin/mcc
    ARGS -o ${exec_name1} 
    -W main:${exec_name1} 
    -T link:exe  
    -d "${builddir}" 
	-N  
	-p stats  
	-w enable:specified_file_mismatch 
	-w enable:repeated_file  
	-w enable:switch_ignored  
	-w enable:missing_lib_sentinel  
	-w enable:demo_license  
	-R -nojvm  
	-R -nodisplay  
	-R -nosplash  
	-v ${sdir}/FEA_MODULE1_HELPER.m 
	-a ${sdir}/analysis.m 
	-a ${sdir}/bisection.m 
	-a ${sdir}/carica_DB.m 
	-a ${sdir}/check_distrizuione.m 
	-a ${sdir}/chi_pval.m 
	-a ${sdir}/closest_corr.m 
	-a ${sdir}/conditional_samps.m 
	-a ${sdir}/copulaName.m 
	-a ${sdir}/copulachoose.m 
	-a ${sdir}/datacorrige2.m 
	-a ${sdir}/estract_cdf.m 
	-a ${sdir}/funzioneB.m 
	-a ${sdir}/funzione.m 
	-a ${sdir}/gausmix3.m 
	-a ${sdir}/gaussian_conditional.m 
	-a ${sdir}/gaussian_mixture.m 
	-a ${sdir}/h_gumbel.m 
	-a ${sdir}/h_inv_ex.m 
	-a ${sdir}/h_inv.m 
	-a ${sdir}/h.m 
	-a ${sdir}/h_pit.m 
	-a ${sdir}/inversion_with_verify2.m 
	-a ${sdir}/invfunzioneB.m 
	-a ${sdir}/licols.m 
	-a ${sdir}/main.m 
	-a ${sdir}/main_MCLA2PC3.m 
	-a ${sdir}/map_ecdf.m 
	-a ${sdir}/MCLA_HELPER.m 
	-a ${sdir}/mod_data.m 
	-a ${sdir}/modelSummary.m 
	-a ${sdir}/modR.m 
	-a ${sdir}/MODULE0.m 
	-a ${sdir}/MODULE1_HELPER.m 
	-a ${sdir}/MODULE1_HELPERS.m 
	-a ${sdir}/MODULE1.m 
	-a ${sdir}/MODULE2_HELPER.m 
	-a ${sdir}/MODULE2_HELPERS.m 
	-a ${sdir}/MODULE2.m 
	-a ${sdir}/MODULE2_OUTPUT2.m 
	-a ${sdir}/MODULE2_OUTPUT.m 
	-a ${sdir}/MODULE3_HELPER.m
	-a ${sdir}/MODULE3_HELPERS.m 
	-a ${sdir}/MODULE3.m 
	-a ${sdir}/MODULE3PRE_HELPERS.m 
	-a ${sdir}/MODULE3_SINGLE.m 
	-a ${sdir}/modU.m 
	-a ${sdir}/new_method_imputation4.m 
	-a ${sdir}/new_method_imputation.m 
	-a ${sdir}/NEW_PIT.m 
	-a ${sdir}/pictures.m 
	-a ${sdir}/rand_gen.m 
	-a ${sdir}/run_module3.m 
	-a ${sdir}/sQuantile.m 
	-a ${sdir}/stimaposdef.m 
	-a ${sdir}/testfilterings2.m 
	-a ${sdir}/trova_closest.m
)


add_custom_command(
    COMMENT "Compiling ${exec_name2}"
    OUTPUT  ${exec_name2}
    DEPENDS ${sdir}/*.m 
    COMMAND ${matlabhome}/bin/mcc
    ARGS -o ${exec_name2} 
    -W main:${exec_name2} 
    -T link:exe  
    -d "${builddir}" 
	-N  
	-p stats  
	-w enable:specified_file_mismatch 
	-w enable:repeated_file  
	-w enable:switch_ignored  
	-w enable:missing_lib_sentinel  
	-w enable:demo_license  
	-R -nojvm  
	-R -nodisplay  
	-R -singleCompThread  
	-R -nosplash  
	-v ${sdir}/FEA_MODULE2_HELPER.m 
	-a ${sdir}/analysis.m 
	-a ${sdir}/bisection.m 
	-a ${sdir}/carica_DB.m 
	-a ${sdir}/check_distrizuione.m 
	-a ${sdir}/chi_pval.m 
	-a ${sdir}/closest_corr.m 
	-a ${sdir}/conditional_samps.m 
	-a ${sdir}/copulaName.m 
	-a ${sdir}/copulachoose.m 
	-a ${sdir}/datacorrige2.m 
	-a ${sdir}/estract_cdf.m 
	-a ${sdir}/funzioneB.m 
	-a ${sdir}/funzione.m 
	-a ${sdir}/gausmix3.m 
	-a ${sdir}/gaussian_conditional.m 
	-a ${sdir}/gaussian_mixture.m 
	-a ${sdir}/h_gumbel.m 
	-a ${sdir}/h_inv_ex.m 
	-a ${sdir}/h_inv.m 
	-a ${sdir}/h.m 
	-a ${sdir}/h_pit.m 
	-a ${sdir}/inversion_with_verify2.m 
	-a ${sdir}/invfunzioneB.m 
	-a ${sdir}/licols.m 
	-a ${sdir}/main.m 
	-a ${sdir}/main_MCLA2PC3.m 
	-a ${sdir}/map_ecdf.m 
	-a ${sdir}/MCLA_HELPER.m 
	-a ${sdir}/mod_data.m 
	-a ${sdir}/modelSummary.m 
	-a ${sdir}/modR.m 
	-a ${sdir}/MODULE0.m 
	-a ${sdir}/MODULE1_HELPER.m 
	-a ${sdir}/MODULE1_HELPERS.m 
	-a ${sdir}/MODULE1.m 
	-a ${sdir}/MODULE2_HELPER.m 
	-a ${sdir}/MODULE2_HELPERS.m 
	-a ${sdir}/MODULE2.m 
	-a ${sdir}/MODULE2_OUTPUT2.m 
	-a ${sdir}/MODULE2_OUTPUT.m 
	-a ${sdir}/MODULE3_HELPER.m
	-a ${sdir}/MODULE3_HELPERS.m 
	-a ${sdir}/MODULE3.m 
	-a ${sdir}/MODULE3PRE_HELPERS.m 
	-a ${sdir}/MODULE3_SINGLE.m 
	-a ${sdir}/modU.m 
	-a ${sdir}/new_method_imputation4.m 
	-a ${sdir}/new_method_imputation.m 
	-a ${sdir}/NEW_PIT.m 
	-a ${sdir}/pictures.m 
	-a ${sdir}/rand_gen.m 
	-a ${sdir}/run_module3.m 
	-a ${sdir}/sQuantile.m 
	-a ${sdir}/stimaposdef.m 
	-a ${sdir}/testfilterings2.m 
	-a ${sdir}/trova_closest.m
)


add_custom_command(
    COMMENT "Compiling ${exec_name3}"
    OUTPUT  ${exec_name3}
    DEPENDS ${sdir}/*.m 
    COMMAND ${matlabhome}/bin/mcc
    ARGS -o ${exec_name3} 
    -W main:${exec_name3} 
    -T link:exe  
    -d "${builddir}" 
	-N  
	-p stats  
	-w enable:specified_file_mismatch 
	-w enable:repeated_file  
	-w enable:switch_ignored  
	-w enable:missing_lib_sentinel  
	-w enable:demo_license  
	-R -nojvm  
	-R -nodisplay  
	-R -singleCompThread  
	-R -nosplash  
	-v ${sdir}/FEA_MODULE2_REDUCE_HELPER.m 
	-a ${sdir}/analysis.m 
	-a ${sdir}/bisection.m 
	-a ${sdir}/carica_DB.m 
	-a ${sdir}/check_distrizuione.m 
	-a ${sdir}/chi_pval.m 
	-a ${sdir}/closest_corr.m 
	-a ${sdir}/conditional_samps.m 
	-a ${sdir}/copulaName.m 
	-a ${sdir}/copulachoose.m 
	-a ${sdir}/datacorrige2.m 
	-a ${sdir}/estract_cdf.m 
	-a ${sdir}/funzioneB.m 
	-a ${sdir}/funzione.m 
	-a ${sdir}/gausmix3.m 
	-a ${sdir}/gaussian_conditional.m 
	-a ${sdir}/gaussian_mixture.m 
	-a ${sdir}/h_gumbel.m 
	-a ${sdir}/h_inv_ex.m 
	-a ${sdir}/h_inv.m 
	-a ${sdir}/h.m 
	-a ${sdir}/h_pit.m 
	-a ${sdir}/inversion_with_verify2.m 
	-a ${sdir}/invfunzioneB.m 
	-a ${sdir}/licols.m 
	-a ${sdir}/main.m 
	-a ${sdir}/main_MCLA2PC3.m 
	-a ${sdir}/map_ecdf.m 
	-a ${sdir}/MCLA_HELPER.m 
	-a ${sdir}/mod_data.m 
	-a ${sdir}/modelSummary.m 
	-a ${sdir}/modR.m 
	-a ${sdir}/MODULE0.m 
	-a ${sdir}/MODULE1_HELPER.m 
	-a ${sdir}/MODULE1_HELPERS.m 
	-a ${sdir}/MODULE1.m 
	-a ${sdir}/MODULE2_HELPER.m 
	-a ${sdir}/MODULE2_HELPERS.m 
	-a ${sdir}/MODULE2.m 
	-a ${sdir}/MODULE2_OUTPUT2.m 
	-a ${sdir}/MODULE2_OUTPUT.m 
	-a ${sdir}/MODULE3_HELPER.m
	-a ${sdir}/MODULE3_HELPERS.m 
	-a ${sdir}/MODULE3.m 
	-a ${sdir}/MODULE3PRE_HELPERS.m 
	-a ${sdir}/MODULE3_SINGLE.m 
	-a ${sdir}/modU.m 
	-a ${sdir}/new_method_imputation4.m 
	-a ${sdir}/new_method_imputation.m 
	-a ${sdir}/NEW_PIT.m 
	-a ${sdir}/pictures.m 
	-a ${sdir}/rand_gen.m 
	-a ${sdir}/run_module3.m 
	-a ${sdir}/sQuantile.m 
	-a ${sdir}/stimaposdef.m 
	-a ${sdir}/testfilterings2.m 
	-a ${sdir}/trova_closest.m
)


add_custom_command(
    COMMENT "Compiling ${exec_name4}"
    OUTPUT  ${exec_name4}
    DEPENDS ${sdir}/*.m 
    COMMAND ${matlabhome}/bin/mcc
    ARGS -o ${exec_name4} 
    -W main:${exec_name4} 
    -T link:exe  
    -d "${builddir}" 
	-N  
	-p stats  
	-w enable:specified_file_mismatch 
	-w enable:repeated_file  
	-w enable:switch_ignored  
	-w enable:missing_lib_sentinel  
	-w enable:demo_license  
	-R -nojvm  
	-R -nodisplay  
	-R -singleCompThread 
	-R -nosplash  
	-v ${sdir}/SAMPLING_MODULE3_HELPER.m 
	-a ${sdir}/analysis.m 
	-a ${sdir}/bisection.m 
	-a ${sdir}/carica_DB.m 
	-a ${sdir}/check_distrizuione.m 
	-a ${sdir}/chi_pval.m 
	-a ${sdir}/closest_corr.m 
	-a ${sdir}/conditional_samps.m 
	-a ${sdir}/copulaName.m 
	-a ${sdir}/copulachoose.m 
	-a ${sdir}/datacorrige2.m 
	-a ${sdir}/estract_cdf.m 
	-a ${sdir}/funzioneB.m 
	-a ${sdir}/funzione.m 
	-a ${sdir}/gausmix3.m 
	-a ${sdir}/gaussian_conditional.m 
	-a ${sdir}/gaussian_mixture.m 
	-a ${sdir}/h_gumbel.m 
	-a ${sdir}/h_inv_ex.m 
	-a ${sdir}/h_inv.m 
	-a ${sdir}/h.m 
	-a ${sdir}/h_pit.m 
	-a ${sdir}/inversion_with_verify2.m 
	-a ${sdir}/invfunzioneB.m 
	-a ${sdir}/licols.m 
	-a ${sdir}/main.m 
	-a ${sdir}/main_MCLA2PC3.m 
	-a ${sdir}/map_ecdf.m 
	-a ${sdir}/MCLA_HELPER.m 
	-a ${sdir}/mod_data.m 
	-a ${sdir}/modelSummary.m 
	-a ${sdir}/modR.m 
	-a ${sdir}/MODULE0.m 
	-a ${sdir}/MODULE1_HELPER.m 
	-a ${sdir}/MODULE1_HELPERS.m 
	-a ${sdir}/MODULE1.m 
	-a ${sdir}/MODULE2_HELPER.m 
	-a ${sdir}/MODULE2_HELPERS.m 
	-a ${sdir}/MODULE2.m 
	-a ${sdir}/MODULE2_OUTPUT2.m 
	-a ${sdir}/MODULE2_OUTPUT.m 
	-a ${sdir}/MODULE3_HELPER.m
	-a ${sdir}/MODULE3_HELPERS.m 
	-a ${sdir}/MODULE3.m 
	-a ${sdir}/MODULE3PRE_HELPERS.m 
	-a ${sdir}/MODULE3_SINGLE.m 
	-a ${sdir}/modU.m 
	-a ${sdir}/new_method_imputation4.m 
	-a ${sdir}/new_method_imputation.m 
	-a ${sdir}/NEW_PIT.m 
	-a ${sdir}/pictures.m 
	-a ${sdir}/rand_gen.m 
	-a ${sdir}/run_module3.m 
	-a ${sdir}/sQuantile.m 
	-a ${sdir}/stimaposdef.m 
	-a ${sdir}/testfilterings2.m 
	-a ${sdir}/trova_closest.m
)

add_custom_command(
    COMMENT "Compiling ${exec_name5}"
    OUTPUT  ${exec_name5}
    DEPENDS ${sdir}/*.m 
    COMMAND ${matlabhome}/bin/mcc
    ARGS -o ${exec_name5} 
    -W main:${exec_name5} 
    -T link:exe  
    -d "${builddir}" 
	-N  
	-p stats  
	-w enable:specified_file_mismatch 
	-w enable:repeated_file  
	-w enable:switch_ignored  
	-w enable:missing_lib_sentinel  
	-w enable:demo_license  
	-R -nojvm  
	-R -nodisplay  
	-R -nosplash  
	-v ${sdir}/MCLA_HELPER.m 
	-a ${sdir}/analysis.m 
	-a ${sdir}/bisection.m 
	-a ${sdir}/carica_DB.m 
	-a ${sdir}/check_distrizuione.m 
	-a ${sdir}/chi_pval.m 
	-a ${sdir}/closest_corr.m 
	-a ${sdir}/conditional_samps.m 
	-a ${sdir}/copulaName.m 
	-a ${sdir}/copulachoose.m 
	-a ${sdir}/datacorrige2.m 
	-a ${sdir}/estract_cdf.m 
	-a ${sdir}/funzioneB.m 
	-a ${sdir}/funzione.m 
	-a ${sdir}/gausmix3.m 
	-a ${sdir}/gaussian_conditional.m 
	-a ${sdir}/gaussian_mixture.m 
	-a ${sdir}/h_gumbel.m 
	-a ${sdir}/h_inv_ex.m 
	-a ${sdir}/h_inv.m 
	-a ${sdir}/h.m 
	-a ${sdir}/h_pit.m 
	-a ${sdir}/inversion_with_verify2.m 
	-a ${sdir}/invfunzioneB.m 
	-a ${sdir}/licols.m 
	-a ${sdir}/main.m 
	-a ${sdir}/main_MCLA2PC3.m 
	-a ${sdir}/map_ecdf.m 
	-a ${sdir}/MCLA_HELPER.m 
	-a ${sdir}/mod_data.m 
	-a ${sdir}/modelSummary.m 
	-a ${sdir}/modR.m 
	-a ${sdir}/MODULE0.m 
	-a ${sdir}/MODULE1_HELPER.m 
	-a ${sdir}/MODULE1_HELPERS.m 
	-a ${sdir}/MODULE1.m 
	-a ${sdir}/MODULE2_HELPER.m 
	-a ${sdir}/MODULE2_HELPERS.m 
	-a ${sdir}/MODULE2.m 
	-a ${sdir}/MODULE2_OUTPUT2.m 
	-a ${sdir}/MODULE2_OUTPUT.m 
	-a ${sdir}/MODULE3_HELPER.m
	-a ${sdir}/MODULE3_HELPERS.m 
	-a ${sdir}/MODULE3.m 
	-a ${sdir}/MODULE3PRE_HELPERS.m 
	-a ${sdir}/MODULE3_SINGLE.m 
	-a ${sdir}/modU.m 
	-a ${sdir}/new_method_imputation4.m 
	-a ${sdir}/new_method_imputation.m 
	-a ${sdir}/NEW_PIT.m 
	-a ${sdir}/pictures.m 
	-a ${sdir}/rand_gen.m 
	-a ${sdir}/run_module3.m 
	-a ${sdir}/sQuantile.m 
	-a ${sdir}/stimaposdef.m 
	-a ${sdir}/testfilterings2.m 
	-a ${sdir}/trova_closest.m
)

add_custom_command(
    COMMENT "Compiling ${exec_name6}"
    OUTPUT  ${exec_name6}
    DEPENDS ${sdir}/*.m 
    COMMAND ${matlabhome}/bin/mcc
    ARGS -o ${exec_name6} 
    -W main:${exec_name6} 
    -T link:exe  
    -d "${builddir}" 
	-N  
	-p stats  
	-w enable:specified_file_mismatch 
	-w enable:repeated_file  
	-w enable:switch_ignored  
	-w enable:missing_lib_sentinel  
	-w enable:demo_license  
	-R -nojvm  
	-R -nodisplay  
	-R -nosplash  
	-v ${sdir}/FPF_HELPER.m 
	-a ${sdir}/analysis.m 
	-a ${sdir}/bisection.m 
	-a ${sdir}/carica_DB.m 
	-a ${sdir}/check_distrizuione.m 
	-a ${sdir}/chi_pval.m 
	-a ${sdir}/closest_corr.m 
	-a ${sdir}/conditional_samps.m 
	-a ${sdir}/copulaName.m 
	-a ${sdir}/copulachoose.m 
	-a ${sdir}/datacorrige2.m 
	-a ${sdir}/estract_cdf.m 
	-a ${sdir}/funzioneB.m 
	-a ${sdir}/funzione.m 
	-a ${sdir}/gausmix3.m 
	-a ${sdir}/gaussian_conditional.m 
	-a ${sdir}/gaussian_mixture.m 
	-a ${sdir}/h_gumbel.m 
	-a ${sdir}/h_inv_ex.m 
	-a ${sdir}/h_inv.m 
	-a ${sdir}/h.m 
	-a ${sdir}/h_pit.m 
	-a ${sdir}/inversion_with_verify2.m 
	-a ${sdir}/invfunzioneB.m 
	-a ${sdir}/licols.m 
	-a ${sdir}/main.m 
	-a ${sdir}/main_MCLA2PC3.m 
	-a ${sdir}/map_ecdf.m 
	-a ${sdir}/MCLA_HELPER.m 
	-a ${sdir}/mod_data.m 
	-a ${sdir}/modelSummary.m 
	-a ${sdir}/modR.m 
	-a ${sdir}/MODULE0.m 
	-a ${sdir}/MODULE1_HELPER.m 
	-a ${sdir}/MODULE1_HELPERS.m 
	-a ${sdir}/MODULE1.m 
	-a ${sdir}/MODULE2_HELPER.m 
	-a ${sdir}/MODULE2_HELPERS.m 
	-a ${sdir}/MODULE2.m 
	-a ${sdir}/MODULE2_OUTPUT2.m 
	-a ${sdir}/MODULE2_OUTPUT.m 
	-a ${sdir}/MODULE3_HELPER.m
	-a ${sdir}/MODULE3_HELPERS.m 
	-a ${sdir}/MODULE3.m 
	-a ${sdir}/MODULE3PRE_HELPERS.m 
	-a ${sdir}/MODULE3_SINGLE.m 
	-a ${sdir}/modU.m 
	-a ${sdir}/new_method_imputation4.m 
	-a ${sdir}/new_method_imputation.m 
	-a ${sdir}/NEW_PIT.m 
	-a ${sdir}/pictures.m 
	-a ${sdir}/rand_gen.m 
	-a ${sdir}/run_module3.m 
	-a ${sdir}/sQuantile.m 
	-a ${sdir}/stimaposdef.m 
	-a ${sdir}/testfilterings2.m 
	-a ${sdir}/trova_closest.m
)


    
add_custom_target(mcla ALL
     COMMENT "building MCLA matlab binaries to ${EXECUTABLE_OUTPUT_PATH}"
     DEPENDS ${exec_name1} ${exec_name2} ${exec_name3} ${exec_name4} ${exec_name5} ${exec_name6}
     COMMAND ${CMAKE_COMMAND} -E copy ${builddir}/${exec_name1} ${EXECUTABLE_OUTPUT_PATH}
     COMMAND ${CMAKE_COMMAND} -E copy ${builddir}/${exec_name2} ${EXECUTABLE_OUTPUT_PATH}
     COMMAND ${CMAKE_COMMAND} -E copy ${builddir}/${exec_name3} ${EXECUTABLE_OUTPUT_PATH}
     COMMAND ${CMAKE_COMMAND} -E copy ${builddir}/${exec_name4} ${EXECUTABLE_OUTPUT_PATH}
     COMMAND ${CMAKE_COMMAND} -E copy ${builddir}/${exec_name5} ${EXECUTABLE_OUTPUT_PATH}
     COMMAND ${CMAKE_COMMAND} -E copy ${builddir}/${exec_name6} ${EXECUTABLE_OUTPUT_PATH}
)

