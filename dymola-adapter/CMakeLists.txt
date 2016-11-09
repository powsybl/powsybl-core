# Copyright (c) 2016, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.


find_package(Matlab REQUIRED)
find_package(Dymola REQUIRED)

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

 set(DYMOLA_SOURCES
    Mfiles/dymtools/dymload.m
    Mfiles/dymtools/dymget.m
)

set(WP43DYMADAPTER_OVERLOAD_SOURCES
    sources/wp43dymadapter_overload.m
)
add_matlab_executable(wp43dymadapter_overload "${WP43DYMADAPTER_OVERLOAD_SOURCES}" "${DYMOLA_SOURCES}")

set(WP43DYMADAPTER_SMALLSIGNAL_SOURCES
    sources/wp43dymadapter_smallsignal.m
)
add_matlab_executable(wp43dymadapter_smallsignal "${WP43DYMADAPTER_SMALLSIGNAL_SOURCES}" "${DYMOLA_SOURCES}")

set(WP43DYMADAPTER_TRANSIENT_SOURCES
    sources/wp43dymadapter_transient.m
)
add_matlab_executable(wp43dymadapter_transient "${WP43DYMADAPTER_TRANSIENT_SOURCES}" "${DYMOLA_SOURCES}")

set(WP43DYMADAPTER_UNDEROVERVOLTAGE_SOURCES
    sources/wp43dymadapter_transient.m
)
add_matlab_executable(wp43dymadapter_underovervoltage "${WP43DYMADAPTER_UNDEROVERVOLTAGE_SOURCES}" "${DYMOLA_SOURCES}")

add_custom_target(dymola_adapters ALL
     COMMENT "building dymola adapters matlab binaries to ${EXECUTABLE_OUTPUT_PATH}"
     DEPENDS wp43dymadapter_overload wp43dymadapter_smallsignal wp43dymadapter_transient wp43dymadapter_underovervoltage
     COMMAND ${CMAKE_COMMAND} -E copy ${CMAKE_CURRENT_BINARY_DIR}/wp43dymadapter_overload ${EXECUTABLE_OUTPUT_PATH}
     COMMAND ${CMAKE_COMMAND} -E copy ${CMAKE_CURRENT_BINARY_DIR}/wp43dymadapter_smallsignal ${EXECUTABLE_OUTPUT_PATH}
     COMMAND ${CMAKE_COMMAND} -E copy ${CMAKE_CURRENT_BINARY_DIR}/wp43dymadapter_transient ${EXECUTABLE_OUTPUT_PATH}
     COMMAND ${CMAKE_COMMAND} -E copy ${CMAKE_CURRENT_BINARY_DIR}/wp43dymadapter_underovervoltage ${EXECUTABLE_OUTPUT_PATH}
     COMMAND ${CMAKE_COMMAND} -E copy ${CMAKE_CURRENT_SOURCE_DIR}/sources/wp43_dymola.sh ${EXECUTABLE_OUTPUT_PATH}
)
