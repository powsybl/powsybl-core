# - Find SuiteSparse
#
# SuiteSparse_FOUND             True if SuiteSparse exists, false otherwise
# SuiteSparse_INCLUDE_DIRS      Include path
# SuiteSparse_LIBRARIES         SuiteSparse libraries
# SuiteSparse_VERSION_STRING    Library version
#
# =============================================================================
# Copyright (c) 2017, RTE (http://www.rte-france.com)
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

if (NOT SUITESPARSE_HOME AND NOT $ENV{SUITESPARSE_HOME} STREQUAL "")
	set(SUITESPARSE_HOME $ENV{SUITESPARSE_HOME})
endif()

if (NOT SUITESPARSE_HOME AND NOT $ENV{SUITESPARSE_ROOT} STREQUAL "")
	set(SUITESPARSE_HOME $ENV{SUITESPARSE_ROOT})
endif()

if (NOT SUITESPARSE_HOME)
    message(FATAL_ERROR "SuiteSparse libraries not found. The variable SUITESPARSE_HOME is NOT set or is NOT a valid directory")
endif()

find_path(SuiteSparse_INCLUDE_DIR NAME SuiteSparse_config.h HINTS ${SUITESPARSE_HOME}/include)
find_library(SuiteSparse_klu_LIBRARY klu HINTS ${SUITESPARSE_HOME}/lib)
find_library(SuiteSparse_cxsparse_LIBRARY cxsparse HINTS ${SUITESPARSE_HOME}/lib)

mark_as_advanced(SuiteSparse_INCLUDE_DIR SuiteSparse_klu_LIBRARY SuiteSparse_cxsparse_LIBRARY)

if (SuiteSparse_INCLUDE_DIR AND EXISTS "${SuiteSparse_INCLUDE_DIR}/SuiteSparse_config.h")
    GetVersionInfo("${SuiteSparse_INCLUDE_DIR}/SuiteSparse_config.h" "^#define[ ^t]+SUITESPARSE_MAIN_VERSION[ \t]+([0-9]+).*$" "SuiteSparse_VERSION_MAIN")
    GetVersionInfo("${SuiteSparse_INCLUDE_DIR}/SuiteSparse_config.h" "^#define[ ^t]+SUITESPARSE_SUB_VERSION[ \t]+([0-9]+).*$" "SuiteSparse_VERSION_SUB")
    GetVersionInfo("${SuiteSparse_INCLUDE_DIR}/SuiteSparse_config.h" "^#define[ ^t]+SUITESPARSE_SUBSUB_VERSION[ \t]+([0-9]+).*$" "SuiteSparse_VERSION_SUBSUB")
    if (SuiteSparse_VERSION_MAIN AND SuiteSparse_VERSION_SUB AND SuiteSparse_VERSION_SUBSUB)
        set(SuiteSparse_VERSION_STRING "${SuiteSparse_VERSION_MAIN}.${SuiteSparse_VERSION_SUB}.${SuiteSparse_VERSION_SUBSUB}")
    endif()
endif()

include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(SuiteSparse DEFAULT_MSG SuiteSparse_klu_LIBRARY SuiteSparse_cxsparse_LIBRARY SuiteSparse_INCLUDE_DIR)
if (SUITESPARSE_FOUND)
    if (DEFINED SuiteSparse_FIND_VERSION)
        if (${SuiteSparse_FIND_VERSION} VERSION_GREATER ${SuiteSparse_VERSION_STRING})
            message(FATAL_ERROR "SuiteSparse ${SuiteSparse_VERSION_STRING} found but ${SuiteSparse_FIND_VERSION} is required")
        endif()
    endif()

    set(SuiteSparse_FOUND ${SUITESPARSE_FOUND})
    set(SuiteSparse_INCLUDE_DIRS ${SuiteSparse_INCLUDE_DIR})
    set(SuiteSparse_LIBRARIES ${SuiteSparse_klu_LIBRARY} ${SuiteSparse_cxsparse_LIBRARY})

    message(STATUS "SuiteSparse version: ${SuiteSparse_VERSION_STRING}")
endif()
