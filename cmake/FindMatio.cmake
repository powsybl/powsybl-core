# - Find Matio
#
# Matio_FOUND             True if Matio exists, false otherwise
# Matio_INCLUDE_DIRS      Include path
# Matio_LIBRARIES         Matio libraries
# Matio_VERSION_STRING    Library version
# Matio_VERSION_MAJOR     Library version (major version)
# Matio_VERSION_MINOR     Library version (minor version)
# Matio_VERSION_PATCH     Library version (patch level)
#
# =============================================================================
# Copyright (c) 2016, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
# =============================================================================

function (GetVersionInfo filename regex varname)
    file(STRINGS ${filename} _VERSION_STRING LIMIT_COUNT 1 REGEX ${regex})
    if (_VERSION_STRING)
        string(REGEX REPLACE "${regex}" "\\1" VERSION_STRING "${_VERSION_STRING}")
    endif()

    set(${varname} ${VERSION_STRING} PARENT_SCOPE)
    unset(VERSION_STRING)
endfunction()


if (NOT MATIO_HOME AND NOT $ENV{MATIO_HOME} STREQUAL "")
	set(MATIO_HOME $ENV{MATIO_HOME})
endif()

if (NOT MATIO_HOME AND NOT $ENV{MATIO_ROOT} STREQUAL "")
	set(MATIO_HOME $ENV{MATIO_ROOT})
endif()

if (NOT MATIO_HOME)
    message(FATAL_ERROR "Matio libraries not found. The variable MATIO_HOME is NOT set or is NOT a valid directory")
endif()

find_path(Matio_INCLUDE_DIR NAME matio.h matio_pubconf.h HINTS ${MATIO_HOME}/include)
if (USE_STATIC_LIBS)
    find_library(Matio_LIBRARY libmatio.a HINTS ${MATIO_HOME}/lib)
else()
    find_library(Matio_LIBRARY matio HINTS ${MATIO_HOME}/lib)
endif()

mark_as_advanced(Matio_INCLUDE_DIR Matio_LIBRARY)

if (Matio_INCLUDE_DIR AND EXISTS "${Matio_INCLUDE_DIR}/matio_pubconf.h")
    GetVersionInfo("${Matio_INCLUDE_DIR}/matio_pubconf.h" "^#define[ ^t]+MATIO_MAJOR_VERSION[ \t]+([0-9]+).*$" "Matio_VERSION_MAJOR")
    GetVersionInfo("${Matio_INCLUDE_DIR}/matio_pubconf.h" "^#define[ ^t]+MATIO_MINOR_VERSION[ \t]+([0-9]+).*$" "Matio_VERSION_MINOR")
    GetVersionInfo("${Matio_INCLUDE_DIR}/matio_pubconf.h" "^#define[ ^t]+MATIO_RELEASE_LEVEL[ \t]+([0-9]+).*$" "Matio_VERSION_PATCH")
    if (Matio_VERSION_MAJOR AND Matio_VERSION_MINOR AND Matio_VERSION_PATCH)
        set(Matio_VERSION_STRING "${Matio_VERSION_MAJOR}.${Matio_VERSION_MINOR}.${Matio_VERSION_PATCH}")
    endif()
endif()

include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(Matio DEFAULT_MSG Matio_LIBRARY Matio_INCLUDE_DIR)
if (MATIO_FOUND)
    if (DEFINED Matio_FIND_VERSION)
        if (${Matio_FIND_VERSION} VERSION_GREATER ${Matio_VERSION_STRING})
            message(FATAL_ERROR "Matio ${Matio_VERSION_STRING} found but ${Matio_FIND_VERSION} is required")
        endif()
    endif()

    set(Matio_FOUND ${MATIO_FOUND})
    set(Matio_INCLUDE_DIRS ${Matio_INCLUDE_DIR})
    set(Matio_LIBRARIES ${Matio_LIBRARY})

    message(STATUS "Matio version: ${Matio_VERSION_STRING}")
endif()
