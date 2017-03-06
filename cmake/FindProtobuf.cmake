# - Find Protobuf
#
# Protobuf_FOUND             True if Protobuf exists, false otherwise
# Protobuf_INCLUDE_DIRS      Include path
# Protobuf_LIBRARIES         Protobuf libraries
# Protobuf_VERSION_STRING    Library version
# Protobuf_VERSION_MAJOR     Library version (major version)
# Protobuf_VERSION_MINOR     Library version (minor version)
# Protobuf_VERSION_PATCH     Library version (patch level)
#
# =============================================================================
# Copyright (c) 2016, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
# =============================================================================

if (NOT PROTOBUF_HOME AND NOT $ENV{PROTOBUF_HOME} STREQUAL "")
    set(PROTOBUF_HOME $ENV{PROTOBUF_HOME})
endif()

if (NOT PROTOBUF_HOME AND NOT $ENV{PROTOBUF_ROOT} STREQUAL "")
    set(PROTOBUF_HOME $ENV{PROTOBUF_ROOT})
endif()

if (NOT PROTOBUF_HOME)
    message(FATAL_ERROR "Protobuf libraries not found. The variable PROTOBUF_HOME is NOT set or is NOT a valid directory")
endif()

find_path(Protobuf_INCLUDE_DIR NAME google/protobuf/service.h HINTS ${PROTOBUF_HOME}/include NO_DEFAULT_PATH)
if (USE_STATIC_LIBS)
    find_library(Protobuf_LIBRARY libprotobuf.a HINTS ${PROTOBUF_HOME}/lib NO_DEFAULT_PATH)
else()
    find_library(Protobuf_LIBRARY protobuf HINTS ${PROTOBUF_HOME}/lib NO_DEFAULT_PATH)
endif()

mark_as_advanced(Protobuf_INCLUDE_DIR Protobuf_LIBRARY)

if (Protobuf_INCLUDE_DIR AND EXISTS "${Protobuf_INCLUDE_DIR}/google/protobuf/stubs/common.h")
    set(_Protobuf_VERSION_REGEX "^#define[ \t]+GOOGLE_PROTOBUF_VERSION[ \t]+([0-9]+).*$")
    file(STRINGS "${Protobuf_INCLUDE_DIR}/google/protobuf/stubs/common.h" _Protobuf_VERSION_STRING LIMIT_COUNT 1 REGEX "${_Protobuf_VERSION_REGEX}")
    if(_Protobuf_VERSION_STRING)
        math(EXPR Protobuf_VERSION_MAJOR "${_Protobuf_VERSION_STRING} / 1000000")
        math(EXPR Protobuf_VERSION_MINOR "${_Protobuf_VERSION_STRING} / 1000 % 1000")
        math(EXPR Protobuf_VERSION_PATCH "${_Protobuf_VERSION_STRING} % 1000")
        set(Protobuf_VERSION_STRING "${Protobuf_VERSION_MAJOR}.${Protobuf_VERSION_MINOR}.${Protobuf_VERSION_PATCH}")
    endif()
    unset(_Protobuf_VERSION_REGEX)
    unset(_Protobuf_VERSION_STRING)
endif()

include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(Protobuf DEFAULT_MSG Protobuf_LIBRARY Protobuf_INCLUDE_DIR)
if (PROTOBUF_FOUND)
    if (DEFINED Protobuf_FIND_VERSION)
        if (Protobuf_FIND_VERSION VERSION_GREATER ${Protobuf_VERSION_STRING})
            message(FATAL_ERROR "Protobuf ${Protobuf_VERSION_STRING} found but ${Protobuf_FIND_VERSION} is required")
        endif()
    endif()

    set(Protobuf_FOUND ${PROTOBUF_FOUND})
    set(Protobuf_INCLUDE_DIRS ${Protobuf_INCLUDE_DIR})
    set(Protobuf_LIBRARIES ${Protobuf_LIBRARY})

    message(STATUS "Protobuf version: ${Protobuf_VERSION_STRING}")
endif()
