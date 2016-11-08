# - Find Matlab
#
# Matlab_FOUND             True if Matlab exists, false otherwise
#
# =============================================================================
# Copyright (c) 2016, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
# =============================================================================

if (NOT MATLAB_HOME AND NOT $ENV{MATLAB_HOME} STREQUAL "")
	set(MATLAB_HOME $ENV{MATLAB_HOME})
endif()

if (NOT MATLAB_HOME AND NOT $ENV{MATLAB_ROOT} STREQUAL "")
	set(MATLAB_HOME $ENV{MATLAB_ROOT})
endif()

if (NOT MATLAB_HOME)
    message(FATAL_ERROR "Matlab not found. The variable MATLAB_HOME is NOT set or is NOT a valid directory")
endif()

find_program(Matlab_COMPILER mcc HINTS ${MATLAB_HOME}/bin)
include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(Matlab "Matlab compiler not found." Matlab_COMPILER)

if (NOT MATLAB_FOUND)
    message(FATAL_ERROR "Matlab compiler not found.")
endif()

# Define a new executable
function (add_matlab_executable exec_name sources)

    # Sources
    foreach (source ${sources})
        if (NOT exec_sources)
            set(exec_sources "-v" "${CMAKE_CURRENT_SOURCE_DIR}/${source}")
        else()
            set(exec_sources ${exec_sources} "-a" "${CMAKE_CURRENT_SOURCE_DIR}/${source}")
        endif()
    endforeach()
    unset(source)

    # Dymola sources
    set (dymola_sources ${ARGN})
    foreach (source ${dymola_sources})
        set(exec_sources ${exec_sources} "-a" "${DYMOLA_HOME}/${source}")
    endforeach()
    unset(source)

    # Runtime options
    set(matlab_runtime_options "")
    if (DEFINED MATLAB_RUNTIME_OPTIONS)
        foreach (option ${MATLAB_RUNTIME_OPTIONS})
            set(matlab_runtime_options ${matlab_runtime_options} "-R" "${option}")
        endforeach()
        unset(option)
    endif()

    # Include directories
    set(matlab_include_directories "-N")
    if (DEFINED MATLAB_INCLUDE_DIRECTORIES)
        foreach (include_directory ${MATLAB_INCLUDE_DIRECTORIES})
            set(matlab_include_directories ${matlab_include_directories} "-p" "${include_directory}")
        endforeach()
        unset(include_directory)
    endif()

    # Warnings
    set(matlab_warning_options "")
    if (DEFINED MATLAB_WARNING_OPTIONS)
        foreach (option ${MATLAB_WARNING_OPTIONS})
            set(matlab_warning_options "${matlab_warning_options}" "-w" "${option}")
        endforeach()
        unset(option)
    endif()

    add_custom_command(
        COMMENT "Compiling ${exec_name}"
        OUTPUT ${exec_name}
        DEPENDS ${sources}
        COMMAND ${MATLAB_HOME}/bin/mcc
        ARGS -o ${exec_name}
        -W main:${exec_name}
        -T link:exe
        -d ${CMAKE_CURRENT_BINARY_DIR}
        ${matlab_include_directories}
        ${matlab_warning_options}
        ${matlab_runtime_options}
        ${exec_sources}
    )

    unset(matlab_include_directories)
    unset(matlab_warning_options)
    unset(matlab_runtime_options)
    unset(exec_sources)

endfunction()

