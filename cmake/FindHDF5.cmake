# - Find HDF5
#
# HDF5_FOUND             True if HDF5 exists, false otherwise
# HDF5_INCLUDE_DIRS      Include path
# HDF5_LIBRARIES         HDF5 libraries
# HDF5_VERSION_STRING    Library version
# HDF5_VERSION_MAJOR     Library version (major version)
# HDF5_VERSION_MINOR     Library version (minor version)
# HDF5_VERSION_PATCH     Library version (patch level)
#
# =============================================================================
# Copyright (c) 2016, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
# =============================================================================

if (NOT HDF5_HOME AND NOT $ENV{HDF5_HOME} STREQUAL "")
	set(HDF5_HOME $ENV{HDF5_HOME})
endif()

if (NOT HDF5_HOME AND NOT $ENV{HDF5_ROOT} STREQUAL "")
	set(HDF5_HOME $ENV{HDF5_ROOT})
endif()

if (NOT HDF5_HOME)
    message(FATAL_ERROR "HDF5 libraries not found. The variable HDF5_HOME is NOT set or is NOT a valid directory")
endif()

find_path(HDF5_INCLUDE_DIR NAME hdf5.h H5pubconf.h HINTS ${HDF5_HOME}/include)
if (USE_STATIC_LIBS)
    find_library(HDF5_LIBRARY libhdf5.a HINTS ${HDF5_HOME}/lib)
else()
    find_library(HDF5_LIBRARY hdf5 HINTS ${HDF5_HOME}/lib)
endif()

mark_as_advanced(HDF5_INCLUDE_DIR HDF5_LIBRARY)

if (HDF5_INCLUDE_DIR AND EXISTS "${HDF5_INCLUDE_DIR}/H5pubconf.h")
    set(_HDF5_VERSION_REGEX "^#define[ ^t]+H5_VERSION[ \t]+\"([^\"]+)\".*$")
    file(STRINGS "${HDF5_INCLUDE_DIR}/H5pubconf.h" _HDF5_VERSION_STRING LIMIT_COUNT 1 REGEX ${_HDF5_VERSION_REGEX})
    if (_HDF5_VERSION_STRING)
        string(REGEX REPLACE "${_HDF5_VERSION_REGEX}" "\\1" HDF5_VERSION_STRING "${_HDF5_VERSION_STRING}")
    endif()
endif()

include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(HDF5 DEFAULT_MSG HDF5_LIBRARY HDF5_INCLUDE_DIR)
if (HDF5_FOUND)
    if (DEFINED HDF5_FIND_VERSION)
        if (${HDF5_FIND_VERSION} VERSION_GREATER ${HDF5_VERSION_STRING})
            message(FATAL_ERROR "HDF5 ${HDF5_VERSION_STRING} found but ${HDF5_FIND_VERSION} is required")
        endif()
    endif()

    set(HDF5_INCLUDE_DIRS ${HDF5_INCLUDE_DIR})
    set(HDF5_LIBRARIES ${HDF5_LIBRARY})

    message(STATUS "HDF5 version: ${HDF5_VERSION_STRING}")
endif()
