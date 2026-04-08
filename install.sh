#!/bin/bash

# Copyright (c) 2016, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

sourceDir=$(dirname $(readlink -f $0))


## install default settings
###############################################################################
powsybl_prefix=$HOME/powsybl
powsybl_mvn=mvn

# Targets
powsybl_clean=false
powsybl_compile=false
powsybl_docs=false
powsybl_install=false

## read settings from configuration file
###############################################################################
settings="$sourceDir/install.cfg"
if [ -f "${settings}" ]; then
     source "${settings}"
fi


## Usage/Help
###############################################################################
cmd=$0
usage() {
    echo "usage: $cmd [options] [target...]"
    echo ""
    echo "Available targets:"
    echo "  clean                    Clean modules"
    echo "  compile                  Compile modules"
    echo "  install                  Compile modules and install it (default target)"
    echo "  help                     Display this help"
    echo "  docs                     Generate the documentation (Doxygen/Javadoc)"
    echo ""
    echo "Options:"
    echo "  --help                   Display this help"
    echo "  --prefix                 Set the installation directory (default is $HOME/powsybl)"
    echo "  --mvn                    Set the maven command to use (default is \"mvn\")"
    echo ""
}


## Write Settings functions
###############################################################################
writeSetting() {
    if [[ $# -lt 2 || $# -gt 3 ]]; then
        echo "WARNING: writeSetting <setting> <value> [comment (true|false)]"
        exit 1
    fi

    SETTING=$1
    VALUE=$2
    if [[ $# -eq 3 ]]; then
        echo -ne "# "
    fi
    echo "${SETTING}=${VALUE}"

    return 0
}

writeComment() {
    echo "# $*"
    return 0
}

writeEmptyLine() {
    echo ""
    return 0
}

writeSettings() {
    writeComment " -- Global options --"
    writeSetting "powsybl_prefix" ${powsybl_prefix}
    writeSetting "powsybl_mvn" ${powsybl_mvn}

    return 0
}

## Build Java Modules
###############################################################################
powsybl_java()
{
    if [[ $powsybl_clean = true || $powsybl_compile = true || $powsybl_docs = true ]]; then
        echo "** Building Java modules"

        mvn_options=""
        [ $powsybl_clean = true ] && mvn_options="$mvn_options clean"
        [ $powsybl_compile = true ] && mvn_options="$mvn_options install"
        if [ ! -z "$mvn_options" ]; then
            "$powsybl_mvn" -f "$sourceDir/pom.xml" $mvn_options || exit $?
        fi

        if [ $powsybl_docs = true ]; then
            echo "**** Generating Javadoc documentation"
            "$powsybl_mvn" -f "$sourceDir/pom.xml" javadoc:aggregate || exit $?
            "$powsybl_mvn" -f "$sourceDir/distribution-core/pom.xml" install || exit $?
        fi
    fi
}

## Install
###############################################################################
powsybl_install()
{
    if [ $powsybl_install = true ]; then
        echo "** Installing powsybl"

        echo "**** Copying files"
        mkdir -p "$powsybl_prefix" || exit $?
        cp -Rp "$sourceDir/distribution-core/target/powsybl"/* "$powsybl_prefix" || exit $?
    fi
}

## Parse command line
###############################################################################
powsybl_options="prefix:,mvn:"

opts=`getopt -o '' --long "help,$powsybl_options" -n 'install.sh' -- "$@"`
eval set -- "$opts"
while true; do
    case "$1" in
        # Options
        --prefix) powsybl_prefix=$2 ; shift 2 ;;
        --mvn) powsybl_mvn=$2 ; shift 2 ;;

        # Help
        --help) usage ; exit 0 ;;

        --) shift ; break ;;
        *) usage ; exit 1 ;;
    esac
done

if [ $# -ne 0 ]; then
    for command in $*; do
        case "$command" in
            clean) powsybl_clean=true ;;
            compile) powsybl_compile=true ;;
            docs) powsybl_docs=true ;;
            install) powsybl_install=true ; powsybl_compile=true ;;
            help) usage; exit 0 ;;
            *) usage ; exit 1 ;;
        esac
    done
else
    powsybl_compile=true
    powsybl_install=true
fi

## Build platform
###############################################################################

# Build Java modules
powsybl_java

# Install
powsybl_install

# Save settings
writeSettings > "${settings}"
