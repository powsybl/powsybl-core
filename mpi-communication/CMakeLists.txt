# Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
# Copyright (c) 2016, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

cmake_minimum_required(VERSION 2.6)

project(mpi)

find_package(MPI REQUIRED)
set(CMAKE_CXX_COMPILER mpicxx)

find_package(Boost REQUIRED COMPONENTS filesystem system program_options iostreams)
find_package(JNI REQUIRED)
find_package(Log4cpp REQUIRED)
find_package(LibArchive REQUIRED)
find_package(Protobuf REQUIRED)
find_package(ZLIB REQUIRED)

# common library
set(COMMON_SOURCES
    src/ioutil.cpp
    src/mpitags.cpp
    src/mpiutil.cpp
)
add_library(common ${COMMON_SOURCES})

# master library
set(MASTER_SOURCES
    src/jniutil.cpp
    src/master.cpp
)
add_library(master SHARED ${MASTER_SOURCES})
target_link_libraries(master ${Boost_SYSTEM_LIBRARY})

# slave binary
set(SLAVE_SOURCES
    src/messages.pb.cc
    src/slave.cpp
)
add_executable(slave ${SLAVE_SOURCES})
target_link_libraries(slave common ${Boost_LIBRARIES} ${LibArchive_LIBRARIES} ${Log4cpp_LIBRARIES} ${Protobuf_LIBRARIES} ${ZLIB_LIBRARIES})

include_directories(${Boost_INCLUDE_DIRS})
include_directories(${JNI_INCLUDE_DIRS})
include_directories(${LibArchive_INCLUDE_DIRS})
include_directories(${Log4cpp_INCLUDE_DIRS})
include_directories(${Protobuf_INCLUDE_DIRS})
