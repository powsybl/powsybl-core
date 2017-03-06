# - Find LibArchive
#
# LibArchive_FOUND             True if LibArchive exists, false otherwise
# LibArchive_INCLUDE_DIRS      Include path
# LibArchive_LIBRARIES         LibArchive libraries
# LibArchive_VERSION_STRING    Library version
# LibArchive_VERSION_MAJOR     Library version (major version)
# LibArchive_VERSION_MINOR     Library version (minor version)
# LibArchive_VERSION_PATCH     Library version (patch level)
#
# =============================================================================
# Copyright (c) 2016, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
# =============================================================================

if (NOT LIBARCHIVE_HOME AND NOT $ENV{LIBARCHIVE_HOME} STREQUAL "")
    set(LIBARCHIVE_HOME $ENV{LIBARCHIVE_HOME})
endif()

if (NOT LIBARCHIVE_HOME AND NOT $ENV{LIBARCHIVE_ROOT} STREQUAL "")
    set(LIBARCHIVE_HOME $ENV{LIBARCHIVE_ROOT})
endif()

if (NOT LIBARCHIVE_HOME)
    message(FATAL_ERROR "LibArchive libraries not found. The variable LIBARCHIVE_HOME is NOT set or is NOT a valid directory")
endif()

find_path(LibArchive_INCLUDE_DIR NAME archive.h archive_entry.h HINTS ${LIBARCHIVE_HOME}/include NO_DEFAULT_PATH)
if (USE_STATIC_LIBS)
    find_library(LibArchive_LIBRARY libarchive.a HINTS ${LIBARCHIVE_HOME}/lib NO_DEFAULT_PATH)
else()
    find_library(LibArchive_LIBRARY archive HINTS ${LIBARCHIVE_HOME}/lib NO_DEFAULT_PATH)
endif()

mark_as_advanced(LibArchive_INCLUDE_DIR LibArchive_LIBRARY)

if (LibArchive_INCLUDE_DIR AND EXISTS "${LibArchive_INCLUDE_DIR}/archive.h")
    # The version string appears in one of two known formats in the header:
    #  #define ARCHIVE_LIBRARY_VERSION "libarchive 2.4.12"
    #  #define ARCHIVE_VERSION_STRING "libarchive 2.8.4"
    # Match either format.
    set(_LibArchive_VERSION_REGEX "^#define[ \t]+ARCHIVE[_A-Z]+VERSION[_A-Z]*[ \t]+\"libarchive +([0-9]+)\\.([0-9]+)\\.([0-9]+)[^\"]*\".*$")
    file(STRINGS "${LibArchive_INCLUDE_DIR}/archive.h" _LibArchive_VERSION_STRING LIMIT_COUNT 1 REGEX "${_LibArchive_VERSION_REGEX}")
    if(_LibArchive_VERSION_STRING)
        string(REGEX REPLACE "${_LibArchive_VERSION_REGEX}" "\\1.\\2.\\3" LibArchive_VERSION_STRING "${_LibArchive_VERSION_STRING}")
        string(REGEX REPLACE "${_LibArchive_VERSION_REGEX}" "\\1" LibArchive_VERSION_MAJOR "${_LibArchive_VERSION_STRING}")
        string(REGEX REPLACE "${_LibArchive_VERSION_REGEX}" "\\2" LibArchive_VERSION_MINOR "${_LibArchive_VERSION_STRING}")
        string(REGEX REPLACE "${_LibArchive_VERSION_REGEX}" "\\3" LibArchive_VERSION_PATCH "${_LibArchive_VERSION_STRING}")
    endif()
    unset(_LibArchive_VERSION_REGEX)
    unset(_LibArchive_VERSION_STRING)
endif()

include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(LibArchive DEFAULT_MSG LibArchive_LIBRARY LibArchive_INCLUDE_DIR)
if (LIBARCHIVE_FOUND)
    if (DEFINED LibArchive_FIND_VERSION)
        if (${LibArchive_FIND_VERSION} VERSION_GREATER ${LibArchive_VERSION_STRING})
            message(FATAL_ERROR "LibArchive ${LibArchive_VERSION_STRING} found but ${LibArchive_FIND_VERSION} is required")
        endif()
    endif()

    set(LibArchive_FOUND ${LIBARCHIVE_FOUND})
    set(LibArchive_INCLUDE_DIRS ${LibArchive_INCLUDE_DIR})
    set(LibArchive_LIBRARIES ${LibArchive_LIBRARY})

    message(STATUS "LibArchive version: ${LibArchive_VERSION_STRING}")
endif()
