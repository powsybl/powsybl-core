# Copyright (c) 2016, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

cmake_minimum_required (VERSION 2.6)

set(exec_name1 wp43_overload)
set(exec_name2 wp43_underovervoltage)
set(exec_name3 wp43_transient)
set(exec_name4 wp43_smallsignal)

set(matlabhome $ENV{MATLABHOME})
set(sdir ${CMAKE_CURRENT_SOURCE_DIR})
set(builddir ${CMAKE_CURRENT_BINARY_DIR})

add_custom_command(
    COMMENT "Compiling ${exec_name1}"
    OUTPUT  ${exec_name1}
    DEPENDS ${sdir}/1_Static_Overload/sources/overload_HELPER.m ${sdir}/1_Static_Overload/sources/static_overload.m
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
	-v ${sdir}/1_Static_Overload/sources/overload_HELPER.m 
        -a ${sdir}/1_Static_Overload/sources/static_overload.m 
)

add_custom_command(
    COMMENT "Compiling ${exec_name2}"
    OUTPUT  ${exec_name2}
    DEPENDS ${sdir}/2_Static_UnderOver_Voltage/sources/underovervoltage_HELPER.m ${sdir}/2_Static_UnderOver_Voltage/sources/static_voltage.m
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
	-v ${sdir}/2_Static_UnderOver_Voltage/sources/underovervoltage_HELPER.m 
        -a ${sdir}/2_Static_UnderOver_Voltage/sources/static_voltage.m 
)

add_custom_command(
    COMMENT "Compiling ${exec_name3}"    
    OUTPUT  ${exec_name3}
    DEPENDS ${sdir}/3_Dynamic_Transient/sources/transient_HELPER.m ${sdir}/3_Dynamic_Transient/sources/dynamic_transient.m
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
	-v ${sdir}/3_Dynamic_Transient/sources/transient_HELPER.m 
        -a ${sdir}/3_Dynamic_Transient/sources/dynamic_transient.m 
)

add_custom_command(
    COMMENT "Compiling ${exec_name4}"
    OUTPUT  ${exec_name4}
    DEPENDS ${sdir}/4_Dynamic_SmallSignal/sources/smallsignal_HELPER.m ${sdir}/4_Dynamic_SmallSignal/sources/pronyiTesla.m ${sdir}/4_Dynamic_SmallSignal/sources/signal_filter.m ${sdir}/4_Dynamic_SmallSignal/sources/sssi.m
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
	-v ${sdir}/4_Dynamic_SmallSignal/sources/smallsignal_HELPER.m 
        -a ${sdir}/4_Dynamic_SmallSignal/sources/pronyiTesla.m
        -a ${sdir}/4_Dynamic_SmallSignal/sources/signal_filter.m
        -a ${sdir}/4_Dynamic_SmallSignal/sources/sssi.m
)



add_custom_target(dynamic_index ALL
     COMMENT "building dynamic indexes matlab binaries to ${EXECUTABLE_OUTPUT_PATH}"
     DEPENDS ${exec_name1} ${exec_name2} ${exec_name3} ${exec_name4}
     COMMAND ${CMAKE_COMMAND} -E copy ${builddir}/${exec_name1} ${EXECUTABLE_OUTPUT_PATH}
     COMMAND ${CMAKE_COMMAND} -E copy ${builddir}/${exec_name2} ${EXECUTABLE_OUTPUT_PATH}
     COMMAND ${CMAKE_COMMAND} -E copy ${builddir}/${exec_name3} ${EXECUTABLE_OUTPUT_PATH}
     COMMAND ${CMAKE_COMMAND} -E copy ${builddir}/${exec_name4} ${EXECUTABLE_OUTPUT_PATH}
)

