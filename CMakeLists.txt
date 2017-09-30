# Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
# Copyright (c) 2016, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

cmake_minimum_required(VERSION 2.6)

project(powsybl)

set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_CURRENT_SOURCE_DIR}/cmake")

set(CMAKE_BINARY_DIR ${CMAKE_SOURCE_DIR}/build)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -g -Wall -std=c++0x -fPIC")

set(EXECUTABLE_OUTPUT_PATH ${CMAKE_BINARY_DIR}/bin)
set(LIBRARY_OUTPUT_PATH ${CMAKE_BINARY_DIR}/lib)

set(USE_STATIC_LIBS true CACHE BOOL "")
set(Boost_USE_STATIC_LIBS ${USE_STATIC_LIBS})

set(CMAKE_VERBOSE_MAKEFILE off)

if (DEFINED thirdparty_prefix AND IS_DIRECTORY "${thirdparty_prefix}")
    message(STATUS "Found thirdparty libraries: ${thirdparty_prefix}")
else()
    message(FATAL_ERROR "Thirdparty libraries not found. The variable thirdparty_prefix is NOT set or is NOT a valid directory.")
endif()
set(PROTOBUF_HOME ${thirdparty_prefix}/protobuf)
set(LIBARCHIVE_HOME ${thirdparty_prefix}/libarchive)
set(BOOST_ROOT ${thirdparty_prefix}/boost)
set(LOG4CPP_HOME ${thirdparty_prefix}/log4cpp)
set(SUITESPARSE_HOME ${thirdparty_prefix}/suitesparse)

add_subdirectory(jni-util)
add_subdirectory(mpi-communication)
add_subdirectory(math-jni)

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

