# Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
# Copyright (c) 2016, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

cmake_minimum_required(VERSION 2.6)

project(itesla)

set(BUILD_MATLAB false CACHE BOOL "")
set(BUILD_EUROSTAG false CACHE BOOL "")
set(BUILD_DYMOLA false CACHE BOOL "")

set(CMAKE_BINARY_DIR ${CMAKE_SOURCE_DIR}/build)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -g -Wall -std=c++0x -fPIC")

set(EXECUTABLE_OUTPUT_PATH ${CMAKE_BINARY_DIR}/bin)
set(LIBRARY_OUTPUT_PATH ${CMAKE_BINARY_DIR}/lib)

set(CMAKE_VERBOSE_MAKEFILE off)

# thirdparty libraries location
if (DEFINED thirdparty_prefix)
    message("thirdparty_prefix=${thirdparty_prefix}")
else()
    message(FATAL_ERROR "thirdparty_prefix is NOT defined")
endif()
set(PROTOBUF_HOME ${thirdparty_prefix}/protobuf)
set(LIBARCHIVE_HOME ${thirdparty_prefix}/libarchive)
set(BOOST_HOME ${thirdparty_prefix}/boost)
set(LOG4CPP_HOME ${thirdparty_prefix}/log4cpp)
set(SZIP_HOME ${thirdparty_prefix}/szip)
set(ZLIB_HOME ${thirdparty_prefix}/zlib)
set(HDF5_HOME ${thirdparty_prefix}/hdf5)
set(MATIO_HOME ${thirdparty_prefix}/matio)

add_subdirectory(mpi-communication)

if(BUILD_EUROSTAG)
 message(STATUS "BUILD_EUROSTAG = " ${BUILD_EUROSTAG} ": eurostag-indexes and wp43adapter binaries will be compiled.")
 add_subdirectory(eurostag-indexes)
 add_subdirectory(wp43adapter)
else()
 message(STATUS "BUILD_EUROSTAG = " ${BUILD_EUROSTAG} ": eurostag-indexes and wp43adapter binaries will not be compiled.")
endif()

if(BUILD_MATLAB)
 message(STATUS "BUILD_MATLAB = " ${BUILD_MATLAB} ": Matlab based components will be compiled.")
 add_subdirectory(sampling)
 add_subdirectory(mcla)
 add_subdirectory(dynamic-indexes)
 if(BUILD_DYMOLA)
    message(STATUS "BUILD_DYMOLA = " ${BUILD_DYMOLA} ": matlab based dymola adapters will be compiled.")
    add_subdirectory(dymola-adapter)
 else()
   message(STATUS "BUILD_DYMOLA = " ${BUILD_DYMOLA} ": matlab based dymola adapters will not be compiled.")
 endif()
else()
 message(STATUS "BUILD_MATLAB = " ${BUILD_MATLAB} ": Matlab based components will not be compiled.")
endif()

find_package(Doxygen)

if(DOXYGEN_FOUND)

    set(doxyfile_in ${CMAKE_CURRENT_SOURCE_DIR}/Doxyfile.in)
    set(doxyfile ${CMAKE_BINARY_DIR}/Doxyfile)

    configure_file(${doxyfile_in} ${doxyfile} @ONLY)

    add_custom_target(doc
            COMMAND ${DOXYGEN_EXECUTABLE} ${doxyfile}
            WORKING_DIRECTORY ${CMAKE_BINARY_DIR}
            COMMENT "Generating API documentation with Doxygen"
            VERBATIM)

endif(DOXYGEN_FOUND)
