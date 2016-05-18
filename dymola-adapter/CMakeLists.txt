# Copyright (c) 2016, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

cmake_minimum_required (VERSION 2.6)

set(exec_name1 wp43dymadapter_overload)
set(exec_name2 wp43dymadapter_smallsignal)
set(exec_name3 wp43dymadapter_transient)
set(exec_name4 wp43dymadapter_underovervoltage)


set(script_file wp43_dymola.sh)

set(matlabhome $ENV{MATLABHOME})
set(dymolahome $ENV{DYMOLAHOME})
set(sdir ${CMAKE_CURRENT_SOURCE_DIR}/sources)
set(ddir ${dymolahome}/Mfiles/dymtools)

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
	-v ${sdir}/wp43dymadapter_overload.m 
    -a ${ddir}/dymload.m
    -a ${ddir}/dymget.m
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
	-v ${sdir}/wp43dymadapter_smallsignal.m 
    -a ${ddir}/dymload.m
    -a ${ddir}/dymget.m
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
	-v ${sdir}/wp43dymadapter_transient.m 
    -a ${ddir}/dymload.m
    -a ${ddir}/dymget.m
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
	-v ${sdir}/wp43dymadapter_underovervoltage.m 
    -a ${ddir}/dymload.m
    -a ${ddir}/dymget.m
    )



add_custom_target(dymola_adapters ALL
     COMMENT "building dymola adapters matlab binaries to ${EXECUTABLE_OUTPUT_PATH}"
     DEPENDS ${exec_name1} ${exec_name2} ${exec_name3} ${exec_name4}
     COMMAND ${CMAKE_COMMAND} -E copy ${builddir}/${exec_name1} ${EXECUTABLE_OUTPUT_PATH}
     COMMAND ${CMAKE_COMMAND} -E copy ${builddir}/${exec_name2} ${EXECUTABLE_OUTPUT_PATH}
     COMMAND ${CMAKE_COMMAND} -E copy ${builddir}/${exec_name3} ${EXECUTABLE_OUTPUT_PATH}
     COMMAND ${CMAKE_COMMAND} -E copy ${builddir}/${exec_name4} ${EXECUTABLE_OUTPUT_PATH}
     COMMAND ${CMAKE_COMMAND} -E copy ${sdir}/${script_file} ${EXECUTABLE_OUTPUT_PATH}
)

