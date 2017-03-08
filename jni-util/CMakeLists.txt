# Copyright (c) 2017, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

cmake_minimum_required(VERSION 2.6)

project(jniutil)

find_package(JNI REQUIRED)

set(SOURCES
    src/jniwrapper.cpp
)
add_library(jniutil STATIC ${SOURCES})

target_include_directories(jniutil
    PUBLIC
    ${JNI_INCLUDE_DIRS}
    $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/src>
)
