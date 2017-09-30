# Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
# Copyright (c) 2016, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

cmake_minimum_required (VERSION 2.6)

project (thirdparty)

include(ExternalProject)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -g -Wall -std=c++0x -fPIC")

set(EXECUTABLE_OUTPUT_PATH ${CMAKE_BINARY_DIR}/bin)
set(LIBRARY_OUTPUT_PATH ${CMAKE_BINARY_DIR}/lib)

set(CMAKE_VERBOSE_MAKEFILE off)

if (NOT download)
    if (NOT DEFINED packs)
        set(packs "$ENV{HOME}/powsybl_packs")
    endif()

    if (NOT IS_DIRECTORY "${packs}")
        message(WARNING "${packs} does not exist or is not a directory. External dependencies will be downloaded.")
        set(download true)
    endif()
endif()

if (NOT DEFINED prefix)
    message(FATAL_ERROR "The 'prefix' option is required.")
endif()

set(CMAKE_BINARY_DIR ${prefix}/build)

set(boost_md5 25f9a8ac28beeb5ab84aa98510305299)
if (download) 
     set(protobuf_url https://github.com/google/protobuf/releases/download/v2.5.0/protobuf-2.5.0.tar.bz2)
     set(libarchive_url http://www.libarchive.org/downloads/libarchive-3.1.2.tar.gz)
     set(boost_url http://sourceforge.net/projects/boost/files/boost/1.57.0/boost_1_57_0.tar.gz/download)
     set(log4cpp_url http://sourceforge.net/projects/log4cpp/files/log4cpp-1.1.x%20%28new%29/log4cpp-1.1/log4cpp-1.1.1.tar.gz)
     set(suitesparse_url http://faculty.cse.tamu.edu/davis/SuiteSparse/SuiteSparse-4.5.4.tar.gz)
else()
    set(protobuf_url ${packs}/protobuf-2.5.0.tar.bz2)
    set(libarchive_url ${packs}/libarchive-3.1.2.tar.gz)
    set(boost_url ${packs}/boost_1_57_0.tar.gz)
    set(log4cpp_url ${packs}/log4cpp-1.1.1.tar.gz)
    set(suitesparse_url ${packs}/SuiteSparse-4.5.4.tar.gz)
endif()


# protobuf
ExternalProject_Add(protobuf_project
    INSTALL_DIR ${prefix}/protobuf
    URL ${protobuf_url}
    CONFIGURE_COMMAND <SOURCE_DIR>/configure --prefix=<INSTALL_DIR> CXXFLAGS=-fPIC LDFLAGS=-fPIC
    BUILD_COMMAND make
    INSTALL_COMMAND make install
)
ExternalProject_Get_Property(protobuf_project install_dir)
set(protobuf_install_dir ${install_dir})

# libarchive
ExternalProject_Add(libarchive_project
    INSTALL_DIR ${prefix}/libarchive
    URL ${libarchive_url}
    CMAKE_ARGS -DCMAKE_INSTALL_PREFIX=<INSTALL_DIR> -DCMAKE_C_FLAGS=-fPIC -DCMAKE_LDFLAGS=-fPIC
)
ExternalProject_Get_Property(libarchive_project install_dir)
set(libarchive_install_dir ${install_dir})

# boost
ExternalProject_Add(boost_project
    INSTALL_DIR ${prefix}/boost
    URL ${boost_url}
    URL_MD5 ${boost_md5}
    UPDATE_COMMAND ""
    CONFIGURE_COMMAND ./bootstrap.sh --with-libraries=system,filesystem,iostreams,program_options
    BUILD_COMMAND ./b2 cxxflags=-fPIC install --prefix=<INSTALL_DIR>
    BUILD_IN_SOURCE 1
    INSTALL_COMMAND ""
)
ExternalProject_Get_Property(boost_project install_dir)
set(boost_install_dir ${install_dir})

# log4cpp
ExternalProject_Add(log4cpp_project
    INSTALL_DIR ${prefix}/log4cpp
    URL ${log4cpp_url}
    CONFIGURE_COMMAND <SOURCE_DIR>/configure --prefix=<INSTALL_DIR> CXXFLAGS=-fPIC LDFLAGS=-fPIC
    BUILD_COMMAND make
    INSTALL_COMMAND make install
)
ExternalProject_Get_Property(log4cpp_project install_dir)
set(log4cpp_install_dir ${install_dir})

# SuiteSparse
ExternalProject_Add(suitesparse_project
    INSTALL_DIR ${prefix}/suitesparse
    URL ${suitesparse_url}
    CONFIGURE_COMMAND ""
    PATCH_COMMAND patch -p0 < ${CMAKE_CURRENT_SOURCE_DIR}/suitesparse.patch
    BUILD_COMMAND make library
    BUILD_IN_SOURCE 1
    INSTALL_COMMAND make install INSTALL=<INSTALL_DIR>
)
ExternalProject_Get_Property(suitesparse_project install_dir)
set(suitesparse_install_dir ${install_dir})
