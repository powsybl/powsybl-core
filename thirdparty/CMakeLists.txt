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

set(download true)

if (DEFINED thirdparty_prefix)
    message("thirdparty_prefix=${thirdparty_prefix}")
else()
    message(FATAL_ERROR "thirdparty_prefix is NOT defined")
endif()

set(CMAKE_BINARY_DIR ${thirdparty_prefix}/build)

set(boost_md5 25f9a8ac28beeb5ab84aa98510305299)
if (download) 
  set(protobuf_url https://protobuf.googlecode.com/files/protobuf-2.5.0.tar.bz2)
  set(libarchive_url http://www.libarchive.org/downloads/libarchive-3.1.2.tar.gz)
  set(boost_url http://sourceforge.net/projects/boost/files/boost/1.57.0/boost_1_57_0.tar.gz/download)
  set(log4cpp_url http://sourceforge.net/projects/log4cpp/files/log4cpp-1.1.x%20%28new%29/log4cpp-1.1/log4cpp-1.1.1.tar.gz)
  set(szip_url http://www.hdfgroup.org/ftp/lib-external/szip/2.1/src/szip-2.1.tar.gz)
  set(zlib_url http://www.hdfgroup.org/ftp/lib-external/zlib/zlib-1.2.5.tar.gz)
  set(hdf5_url http://www.hdfgroup.org/ftp/HDF5//releases/hdf5-1.8.13/src/hdf5-1.8.13.tar.gz)
  set(matio_url http://downloads.sourceforge.net/project/matio/matio/1.5.2/matio-1.5.2.tar.gz)
#  set(openmpi_url http://www.open-mpi.org/software/ompi/v1.8/downloads/openmpi-1.8.3.tar.bz2)
else()
  set(protobuf_url $ENV{HOME}/pack/protobuf-2.5.0.tar.bz2)
  set(libarchive_url $ENV{HOME}/pack/libarchive-3.1.2.tar.gz)
  set(boost_url $ENV{HOME}/pack/boost_1_57_0.tar.gz)
  set(log4cpp_url $ENV{HOME}/pack/log4cpp-1.1.1.tar.gz)
  set(szip_url $ENV{HOME}/pack/szip-2.1.tar.gz)
  set(zlib_url $ENV{HOME}/pack/zlib-1.2.5.tar.gz)
  set(hdf5_url $ENV{HOME}/pack/hdf5-1.8.13.tar.gz)
  set(matio_url $ENV{HOME}/pack/matio-1.5.2.tar.gz)
#  set(openmpi_url $ENV{HOME}/pack/openmpi-1.8.3.tar.bz2)
endif()


# protobuf
ExternalProject_Add(protobuf_project
  INSTALL_DIR ${thirdparty_prefix}/protobuf
  URL ${protobuf_url}
  CONFIGURE_COMMAND <SOURCE_DIR>/configure --prefix=<INSTALL_DIR> CXXFLAGS=-fPIC LDFLAGS=-fPIC
  BUILD_COMMAND make
  INSTALL_COMMAND make install
)
ExternalProject_Get_Property(protobuf_project install_dir)
set(protobuf_install_dir ${install_dir})

# libarchive
ExternalProject_Add(libarchive_project
  INSTALL_DIR ${thirdparty_prefix}/libarchive
  URL ${libarchive_url}
  CMAKE_ARGS -DCMAKE_INSTALL_PREFIX=<INSTALL_DIR> -DCMAKE_C_FLAGS=-fPIC -DCMAKE_LDFLAGS=-fPIC
)
ExternalProject_Get_Property(libarchive_project install_dir)
set(libarchive_install_dir ${install_dir})

# boost
ExternalProject_Add(boost_project
  INSTALL_DIR ${thirdparty_prefix}/boost
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
  INSTALL_DIR ${thirdparty_prefix}/log4cpp
  URL ${log4cpp_url}
  CONFIGURE_COMMAND <SOURCE_DIR>/configure --prefix=<INSTALL_DIR> CXXFLAGS=-fPIC LDFLAGS=-fPIC
  BUILD_COMMAND make
  INSTALL_COMMAND make install
)
ExternalProject_Get_Property(log4cpp_project install_dir)
set(log4cpp_install_dir ${install_dir})

# szip
ExternalProject_Add(szip_project
  INSTALL_DIR ${thirdparty_prefix}/szip
  URL ${szip_url}
  CMAKE_ARGS -DCMAKE_INSTALL_PREFIX=<INSTALL_DIR> -DCMAKE_CXX_FLAGS=-fPIC -DCMAKE_LDFLAGS=-fPIC
)
ExternalProject_Get_Property(szip_project install_dir)
set(szip_install_dir ${install_dir})

# zlib
ExternalProject_Add(zlib_project
  INSTALL_DIR ${thirdparty_prefix}/zlib
  URL ${zlib_url}
  CMAKE_ARGS -DCMAKE_INSTALL_PREFIX=<INSTALL_DIR> -DCMAKE_C_FLAGS=-fPIC -DCMAKE_LDFLAGS=-fPIC
)
ExternalProject_Get_Property(zlib_project install_dir)
set(zlib_install_dir ${install_dir})

# HDF5
ExternalProject_Add(hdf5_project
  DEPENDS szip_project zlib_project
  INSTALL_DIR ${thirdparty_prefix}/hdf5
  URL ${hdf5_url}
  CONFIGURE_COMMAND <SOURCE_DIR>/configure --prefix=<INSTALL_DIR> --with-zlib=${zlib_install_dir} --with-szlib=${szip_install_dir} CXXFLAGS=-fPIC LDFLAGS=-fPIC
  BUILD_COMMAND make
  INSTALL_COMMAND make install
)
ExternalProject_Get_Property(hdf5_project install_dir)
set(hdf5_install_dir ${install_dir})

# matio
ExternalProject_Add(matio_project
  DEPENDS hdf5_project
  INSTALL_DIR ${thirdparty_prefix}/matio
  URL ${matio_url}
  CONFIGURE_COMMAND <SOURCE_DIR>/configure --prefix=<INSTALL_DIR> --with-zlib=${zlib_install_dir} --with-hdf5=${hdf5_install_dir} CXXFLAGS=-fPIC LDFLAGS=-fPIC
  BUILD_COMMAND make
  INSTALL_COMMAND make install
)
ExternalProject_Get_Property(matio_project install_dir)
set(matio_install_dir ${install_dir})

# openmpi: is assumbed to be already installed on target OS
#ExternalProject_Add(openmpi_project
#  INSTALL_DIR ${thirdparty_prefix}/openmpi
#  URL ${openmpi_url}
#  CONFIGURE_COMMAND <SOURCE_DIR>/configure --prefix=<INSTALL_DIR> --enable-mpi-thread-multiple
#  BUILD_COMMAND make
#  INSTALL_COMMAND make install
#)
#ExternalProject_Get_Property(openmpi_project install_dir)
#set(openmpi_install_dir ${install_dir})
