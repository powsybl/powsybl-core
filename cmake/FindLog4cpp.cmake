# - Find Log4cpp
#
# Log4cpp_FOUND             True if Log4cpp exists, false otherwise
# Log4cpp_INCLUDE_DIRS      Include path
# Log4cpp_LIBRARIES         Log4cpp libraries
# Log4cpp_VERSION_STRING    Library version
#
# =============================================================================
# Copyright (c) 2016, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
# =============================================================================

if (NOT LOG4CPP_HOME AND NOT $ENV{LOG4CPP_HOME} STREQUAL "")
    set(LOG4CPP_HOME $ENV{LOG4CPP_HOME})
endif()

if (NOT LOG4CPP_HOME AND NOT $ENV{LOG4CPP_ROOT} STREQUAL "")
    set(LOG4CPP_HOME $ENV{LOG4CPP_ROOT})
endif()

if (NOT LOG4CPP_HOME)
    message(FATAL_ERROR "Log4Cpp libraries not found. The variable LOG4CPP_HOME is NOT set or is NOT a valid directory")
endif()

find_path(Log4cpp_INCLUDE_DIR NAME log4cpp/Category.hh log4cpp/Appender.hh HINTS ${LOG4CPP_HOME}/include NO_DEFAULT_PATH)
if (USE_STATIC_LIBS)
    find_library(Log4cpp_LIBRARY liblog4cpp.a HINTS ${LOG4CPP_HOME}/lib NO_DEFAULT_PATH)
else()
    find_library(Log4cpp_LIBRARY log4cpp HINTS ${LOG4CPP_HOME}/lib NO_DEFAULT_PATH)
endif()

mark_as_advanced(Log4cpp_INCLUDE_DIR Log4cpp_LIBRARY)

if (Log4cpp_INCLUDE_DIR AND EXISTS "${Log4cpp_INCLUDE_DIR}/log4cpp/config.h")
    set(_Log4cpp_VERSION_REGEX "^#define[ \t]+LOG4CPP_VERSION[ \t]+\"(.*)\".*$")
    file(STRINGS "${Log4cpp_INCLUDE_DIR}/log4cpp/config.h" _Log4cpp_VERSION_STRING LIMIT_COUNT 1 REGEX "${_Log4cpp_VERSION_REGEX}")
    if (_Log4cpp_VERSION_STRING)
        string(REGEX REPLACE "${_Log4cpp_VERSION_REGEX}" "\\1" Log4cpp_VERSION_STRING "${_Log4cpp_VERSION_STRING}")
    endif()
    unset(_Log4cpp_VERSION_REGEX)
    unset(_Log4cpp_VERSION_STRING)
endif()

include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(Log4cpp DEFAULT_MSG Log4cpp_LIBRARY Log4cpp_INCLUDE_DIR)
if (LOG4CPP_FOUND)
    if (DEFINED Log4cpp_FIND_VERSION)
        if (${Log4cpp_FIND_VERSION} VERSION_GREATER ${Log4cpp_VERSION_STRING})
            message(FATAL_ERROR "Log4cpp ${Log4cpp_VERSION_STRING} found but ${Log4cpp_FIND_VERSION} is required")
        endif()
    endif()

    set(Log4cpp_FOUND ${LOG4CPP_FOUND})
    set(Log4cpp_INCLUDE_DIRS ${Log4cpp_INCLUDE_DIR})
    set(Log4cpp_LIBRARIES ${Log4cpp_LIBRARY})

    message(STATUS "Log4cpp version: ${Log4cpp_VERSION_STRING}")
endif()
