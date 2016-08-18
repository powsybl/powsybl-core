# Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
# Copyright (c) 2016, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

cmake_minimum_required(VERSION 2.6)

project(indexes)

find_package(Boost 1.57 REQUIRED)
find_package(Eurostag REQUIRED)
find_package(IntelFortran REQUIRED COMPONENTS ifcore ifport imf intlc)

# tsoindexes executable
set(TSOINDEXES_SOURCES
    src/TimeSeries.cpp
    src/EurostagTimeSeries.cpp
    src/tsoindexes.cpp
)
add_executable(tsoindexes ${TSOINDEXES_SOURCES})
target_link_libraries(tsoindexes ${Eurostag_LIBRARIES} ${IntelFortran_LIBRARIES} ${CMAKE_DL_LIBS})

# tsextract executable
set(TSEXTRACT_SOURCES
    src/TimeSeries.cpp
    src/EurostagTimeSeries.cpp
    src/tsextract.cpp
)
add_executable(tsextract ${TSEXTRACT_SOURCES})
message(STATUS, "ESG-Libs: ${Eurostag_LIBRARIES}")
target_link_libraries(tsextract ${Eurostag_LIBRARIES} ${IntelFortran_LIBRARIES} ${CMAKE_DL_LIBS})

include_directories(${Boost_INCLUDE_DIRS})
include_directories(${Eurostag_INCLUDE_DIRS})
