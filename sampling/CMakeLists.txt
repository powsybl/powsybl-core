# Copyright (c) 2016, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

cmake_minimum_required (VERSION 2.6)

set(exec_name1 wp41c_v67_m1)
set(exec_name2 wp41c_v67_m2)
set(exec_name3 wp41c_v67_m3pre)
set(exec_name4 wp41c_v67_m3)
set(exec_name5 wp41c_v67_aggregator)

set(matlabhome $ENV{MATLABHOME})
set(sdir ${CMAKE_CURRENT_SOURCE_DIR}/sources)
set(builddir ${CMAKE_CURRENT_BINARY_DIR})

add_custom_command(
    COMMENT "Compiling ${exec_name1}"
    OUTPUT  ${exec_name1}
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
	-R -singleCompThread  
	-R -nosplash  
	-v ${sdir}/MODULE1_HELPERS.m  
	-a ${sdir}/MODULE1_HELPER.m
	-a ${sdir}/MODULE1.m
)

add_custom_command(
    COMMENT "Compiling ${exec_name2}"
    OUTPUT  ${exec_name2}
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
	-v ${sdir}/MODULE2_HELPERS.m 
    -a ${sdir}/MODULE2_HELPER.m 
    -a ${sdir}/MODULE2.m 
    -a ${sdir}/h.m 
    -a ${sdir}/NEW_PIT.m 
    -a ${sdir}/copulachoose.m 
    -a ${sdir}/map_ecdf.m 
    -a ${sdir}/copulaName.m 
    -a ${sdir}/modU.m
)

add_custom_command(
    COMMENT "Compiling ${exec_name3}"
    OUTPUT  ${exec_name3}
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
	-v ${sdir}/MODULE3PRE_HELPERS.m 
)

add_custom_command(
    COMMENT "Compiling ${exec_name4}"
    OUTPUT  ${exec_name4}
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
	-v ${sdir}/MODULE3_HELPERS.m
    -a ${sdir}/MODULE3_HELPER.m
    -a ${sdir}/MODULE3.m
    -a ${sdir}/h_inv_ex.m
    -a ${sdir}/modU.m
    -a ${sdir}/sQuantile.m 
)

add_custom_command(
    COMMENT "Compiling ${exec_name5}"
    OUTPUT  ${exec_name5}
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
	-R -singleCompThread  
	-R -nosplash  
	-v ${sdir}/AGGREGATORS.m
 	-a ${sdir}/AGGREGATOR.m
 	-a ${sdir}/insert_ind.m 
)

    
add_custom_target(sampling ALL
     COMMENT "building sampling matlab binaries to ${EXECUTABLE_OUTPUT_PATH}"
     DEPENDS ${exec_name1} ${exec_name2} ${exec_name3} ${exec_name4} ${exec_name5}
     COMMAND ${CMAKE_COMMAND} -E copy ${builddir}/${exec_name1} ${EXECUTABLE_OUTPUT_PATH}
     COMMAND ${CMAKE_COMMAND} -E copy ${builddir}/${exec_name2} ${EXECUTABLE_OUTPUT_PATH}
     COMMAND ${CMAKE_COMMAND} -E copy ${builddir}/${exec_name3} ${EXECUTABLE_OUTPUT_PATH}
     COMMAND ${CMAKE_COMMAND} -E copy ${builddir}/${exec_name4} ${EXECUTABLE_OUTPUT_PATH}
     COMMAND ${CMAKE_COMMAND} -E copy ${builddir}/${exec_name5} ${EXECUTABLE_OUTPUT_PATH}
)

