# Copyright (c) 2016, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

find_package(Matlab REQUIRED)

set(MATLAB_INCLUDE_DIRECTORIES
    stats
)

set(MATLAB_RUNTIME_OPTIONS
	-nojvm
	-nodisplay
	-singleCompThread
	-nosplash
)

set(MATLAB_WARNING_OPTIONS
	enable:specified_file_mismatch
	enable:repeated_file
	enable:switch_ignored
	enable:missing_lib_sentinel
	enable:demo_license
)

set(WP43_OVERLOAD_SOURCES
	1_Static_Overload/sources/overload_HELPER.m
    1_Static_Overload/sources/static_overload.m
)
add_matlab_executable(wp43_overload "${WP43_OVERLOAD_SOURCES}")

set(WP43_UNDEROVERVOLTAGE_SOURCES
	2_Static_UnderOver_Voltage/sources/underovervoltage_HELPER.m
    2_Static_UnderOver_Voltage/sources/static_voltage.m
)
add_matlab_executable(wp43_underovervoltage "${WP43_UNDEROVERVOLTAGE_SOURCES}")

set(WP43_TRANSIENT_SOURCES
	3_Dynamic_Transient/sources/transient_HELPER.m
    3_Dynamic_Transient/sources/dynamic_transient.m
)
add_matlab_executable(wp43_transient "${WP43_TRANSIENT_SOURCES}")

set(WP43_SMALLSIGNAL_SOURCES
	4_Dynamic_SmallSignal/sources/smallsignal_HELPER.m
    4_Dynamic_SmallSignal/sources/pronyiTesla.m
    4_Dynamic_SmallSignal/sources/signal_filter.m
    4_Dynamic_SmallSignal/sources/sssi.m
)
add_matlab_executable(wp43_smallsignal "${WP43_SMALLSIGNAL_SOURCES}")

add_custom_target(dynamic_index ALL
     COMMENT "building dynamic indexes matlab binaries to ${EXECUTABLE_OUTPUT_PATH}"
     DEPENDS wp43_overload wp43_underovervoltage wp43_transient wp43_smallsignal
     COMMAND ${CMAKE_COMMAND} -E copy ${CMAKE_CURRENT_BINARY_DIR}/wp43_overload ${EXECUTABLE_OUTPUT_PATH}
     COMMAND ${CMAKE_COMMAND} -E copy ${CMAKE_CURRENT_BINARY_DIR}/wp43_underovervoltage ${EXECUTABLE_OUTPUT_PATH}
     COMMAND ${CMAKE_COMMAND} -E copy ${CMAKE_CURRENT_BINARY_DIR}/wp43_transient ${EXECUTABLE_OUTPUT_PATH}
     COMMAND ${CMAKE_COMMAND} -E copy ${CMAKE_CURRENT_BINARY_DIR}/wp43_smallsignal ${EXECUTABLE_OUTPUT_PATH}
)
