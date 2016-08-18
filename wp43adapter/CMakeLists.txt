# Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
# Copyright (c) 2016, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

# Find external packages
find_package(Boost REQUIRED)
find_package(Eurostag REQUIRED)
find_package(HDF5)
find_package(IntelFortran REQUIRED COMPONENTS ifcore ifport imf intlc)
find_package(Log4cpp REQUIRED)
find_package(Matio REQUIRED)
find_package(Threads)
find_package(ZLIB REQUIRED)

# wp43adapter
set(WP43ADAPTER_SOURCES
    src/Chameleon.cpp
    src/ConfigFile.cpp
    src/wp43adapter.cpp
)
add_executable(wp43adapter ${WP43ADAPTER_SOURCES})
target_link_libraries(wp43adapter ${Eurostag_LIBRARIES} ${IntelFortran_LIBRARIES} ${Log4cpp_LIBRARIES} ${Matio_LIBRARIES} ${HDF5_LIBRARIES} ${ZLIB_LIBRARIES} ${CMAKE_THREAD_LIBS_INIT} ${CMAKE_DL_LIBS})

include_directories(${Boost_INCLUDE_DIRS})
include_directories(${Eurostag_INCLUDE_DIRS})
include_directories(${Log4cpp_INCLUDE_DIRS})
include_directories(${Matio_INCLUDE_DIRS})

#add the wp43_eurostag.sh script to the output path
add_custom_target(copy_scripts ALL
    DEPENDS wp43adapter
    COMMAND ${CMAKE_COMMAND} -E copy ${CMAKE_CURRENT_SOURCE_DIR}/src/wp43_eurostag.sh ${EXECUTABLE_OUTPUT_PATH}
)
