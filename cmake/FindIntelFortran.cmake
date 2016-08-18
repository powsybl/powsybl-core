# - Find Intel Fortran
#
# IntelFortran_FOUND             True if Intel Fortran exists, false otherwise
# IntelFortran_INCLUDE_DIRS      Include path
# IntelFortran_LIBRARIES         Fortran libraries
#
# =============================================================================
# Copyright (c) 2016, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
# =============================================================================

if (NOT INTEL_HOME AND NOT $ENV{INTEL_HOME} STREQUAL "")
	set(INTEL_HOME $ENV{INTEL_HOME})
endif()

if (NOT INTEL_HOME AND NOT $ENV{INTEL_ROOT} STREQUAL "")
	set(INTEL_HOME $ENV{INTEL_ROOT})
endif()

if (NOT INTEL_HOME)
    message(FATAL_ERROR "Intel Fortran libraries not found. The variable INTEL_HOME is NOT set or is NOT a valid directory")
endif()

include(FindPackageHandleStandardArgs)
foreach (component ${IntelFortran_FIND_COMPONENTS})
    string(TOUPPER ${component} COMPONENT)
    set(IntelFortran_${component}_FIND_QUIETLY true)

    find_library(IntelFortran_${component}_LIBRARY ${component} HINTS ${INTEL_HOME}/lib/intel64)
    mark_as_advanced(IntelFortran_${component}_LIBRARY)
    find_package_handle_standard_args(IntelFortran_${component} DEFAULT_MSG IntelFortran_${component}_LIBRARY)

    if (INTELFORTRAN_${COMPONENT}_FOUND)
        if (IntelFortran_LIBRARIES)
            set(IntelFortran_LIBRARIES ${IntelFortran_LIBRARIES} ${IntelFortran_${component}_LIBRARY})
        else()
            set(IntelFortran_LIBRARIES ${IntelFortran_${component}_LIBRARY})
        endif()
        set(IntelFortran_${COMPONENT}_FOUND true)
    elseif (INTELFORTRAN_${COMPONENT}_REQUIRE)
         message(FATAL_ERROR "Required Intel Fortran component ${component} not found")
    endif()

    unset(INTELFORTRAN_${COMPONENT}_FOUND)
endforeach()

set(IntelFortran_FOUND true)
message(STATUS "Found the following Intel Fortran libraries:")
foreach (component ${IntelFortran_FIND_COMPONENTS})
    string(TOUPPER ${component} COMPONENT)
    if (DEFINED IntelFortran_${COMPONENT}_FOUND)
        message(STATUS "  ${component}\t\t(${IntelFortran_${component}_LIBRARY})")
    endif()
endforeach()
unset(COMPONENT)
