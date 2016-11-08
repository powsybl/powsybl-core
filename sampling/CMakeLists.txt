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

set(WP41C_V67_M1_SOURCES
    sources/MODULE1_HELPERS.m
	sources/MODULE1_HELPER.m
	sources/MODULE1.m
)
add_matlab_executable(wp41c_v67_m1 "${WP41C_V67_M1_SOURCES}")

set(WP41C_V67_M2_SOURCES
    sources/MODULE2_HELPERS.m
    sources/MODULE2_HELPER.m
    sources/MODULE2.m
    sources/h.m
    sources/NEW_PIT.m
    sources/copulachoose.m
    sources/map_ecdf.m
    sources/copulaName.m
    sources/modU.m
)
add_matlab_executable(wp41c_v67_m2 "${WP41C_V67_M2_SOURCES}")

set(WP41C_V67_M3PRE_SOURCES
    sources/MODULE3PRE_HELPERS.m
)
add_matlab_executable(wp41c_v67_m3pre "${WP41C_V67_M3PRE_SOURCES}")

set(WP41C_V67_M3_SOURCES
	sources/MODULE3_HELPERS.m
    sources/MODULE3_HELPER.m
    sources/MODULE3.m
    sources/h_inv_ex.m
    sources/modU.m
    sources/sQuantile.m
)
add_matlab_executable(wp41c_v67_m3 "${WP41C_V67_M3_SOURCES}")

set(WP41C_V67_AGGREGATOR_SOURCES
    sources/AGGREGATORS.m
    sources/AGGREGATOR.m
    sources/insert_ind.m
)
add_matlab_executable(wp41c_v67_aggregator "${WP41C_V67_AGGREGATOR_SOURCES}")

add_custom_target(sampling ALL
     COMMENT "building sampling matlab binaries to ${EXECUTABLE_OUTPUT_PATH}"
     DEPENDS wp41c_v67_m1 wp41c_v67_m2 wp41c_v67_m3pre wp41c_v67_m3 wp41c_v67_aggregator
     COMMAND ${CMAKE_COMMAND} -E copy ${CMAKE_CURRENT_BINARY_DIR}/wp41c_v67_m1 ${EXECUTABLE_OUTPUT_PATH}
     COMMAND ${CMAKE_COMMAND} -E copy ${CMAKE_CURRENT_BINARY_DIR}/wp41c_v67_m2 ${EXECUTABLE_OUTPUT_PATH}
     COMMAND ${CMAKE_COMMAND} -E copy ${CMAKE_CURRENT_BINARY_DIR}/wp41c_v67_m3pre ${EXECUTABLE_OUTPUT_PATH}
     COMMAND ${CMAKE_COMMAND} -E copy ${CMAKE_CURRENT_BINARY_DIR}/wp41c_v67_m3 ${EXECUTABLE_OUTPUT_PATH}
     COMMAND ${CMAKE_COMMAND} -E copy ${CMAKE_CURRENT_BINARY_DIR}/wp41c_v67_aggregator ${EXECUTABLE_OUTPUT_PATH}
)

