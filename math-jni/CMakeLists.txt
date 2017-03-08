# Copyright (c) 2017, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

cmake_minimum_required(VERSION 2.6)

project(mathjni)

find_package(JNI REQUIRED)
find_package(SuiteSparse REQUIRED)

set(SOURCES
    src/lu.cpp
)
add_library(mathjni SHARED ${SOURCES})

target_include_directories(mathjni
    PUBLIC
    jniutil
    ${JNI_INCLUDE_DIRS}
    ${SuiteSparse_INCLUDE_DIRS}
)

target_link_libraries(mathjni
    jniutil
    ${SuiteSparse_LIBRARIES}
)
