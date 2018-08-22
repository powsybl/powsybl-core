#!/bin/bash

# Copyright (c) 2016, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

sourceDir=$(dirname $(readlink -f $0))


## install default settings
###############################################################################
powsybl_prefix=$HOME/powsybl

thirdparty_build=true
thirdparty_prefix=$HOME/powsybl_thirdparty
thirdparty_download=true
thirdparty_packs=$HOME/powsybl_packs

# Targets
powsybl_clean=false
powsybl_compile=false
powsybl_docs=false
powsybl_install=false
thirdparty_clean=false


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
    echo "  clean-thirdparty         Clean the thirdparty libraries"
    echo "  compile                  Compile modules"
    echo "  install                  Compile modules and install it (default target)"
    echo "  help                     Display this help"
    echo "  docs                     Generate the documentation (Doxygen/Javadoc)"
    echo ""
    echo "Options:"
    echo "  --help                   Display this help"
    echo "  --prefix                 Set the installation directory (default is $HOME/powsybl)"
    echo ""
    echo "Thirdparty options:"
    echo "  --with-thirdparty        Enable the compilation of thirdparty libraries (default)"
    echo "  --without-thirdparty     Disable the compilation of thirdparty libraries"
    echo "  --thirdparty-prefix      Set the thirdparty installation directory (default is $HOME/powsybl_thirdparty)"
    echo "  --thirdparty-download    Sets false to compile thirdparty libraries from a local repository (default is true)"
    echo "  --thirdparty-packs       Sets the thirdparty libraries local repository (default is $HOME/powsybl_packs)"
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

    writeEmptyLine

    writeComment " -- Thirdparty libraries --"
    writeSetting "thirdparty_build" ${thirdparty_build}
    writeSetting "thirdparty_prefix" ${thirdparty_prefix}
    writeSetting "thirdparty_download" ${thirdparty_download}
    writeSetting "thirdparty_packs" ${thirdparty_packs}

    return 0
}


## Build required C++ thirdparty libraries
###############################################################################
buildthirdparty()
{
    if [[ $thirdparty_clean = true || $powsybl_compile = true ]]; then
        echo "** C++ thirdparty libraries"

        if [ $thirdparty_clean = true ]; then
            echo "**** Removing thirdparty install directory (if already exists)."
            rm -rf "$thirdparty_prefix"
        fi

        if [ $powsybl_compile = true ]; then
            if [ $thirdparty_build = true ]; then
                 echo "**** Building the C++ thirdparty libraries"
                 thirdparty_builddir="${thirdparty_prefix}/build/powsybl-core"
                 cmake -Dprefix="$thirdparty_prefix" -Ddownload="$thirdparty_download" -Dpacks="$thirdparty_packs" -G "Unix Makefiles" -H"$sourceDir/thirdparty" -B"$thirdparty_builddir" || exit $?
                 make -C "$thirdparty_builddir" || exit $?

                 thirdparty_build=false
            else
                echo "**** Skipping the build of the required thirdparty libraries, assuming a previous build in $thirdparty_prefix"
            fi
        fi
    fi
}

## Build C++ modules
###############################################################################
powsybl_cpp()
{
    if [[ $powsybl_clean = true || $powsybl_compile = true || $powsybl_docs = true ]]; then
        echo "** C++ modules"

        powsybl_builddir=$sourceDir/build

        if [ $powsybl_clean = true ]; then
            echo "**** Removing build directory (if already exists)."
            rm -rf "$powsybl_builddir"
        fi

        if [[ $powsybl_compile = true || $powsybl_docs = true ]]; then
            # TODO: rename variable
            cmake -DCMAKE_INSTALL_PREFIX="$powsybl_prefix" -Dthirdparty_prefix="$thirdparty_prefix" -G "Unix Makefiles" -H"$sourceDir" -B"$powsybl_builddir" || exit $?

            if [ $powsybl_compile = true ]; then
                echo "**** Compiling C++ modules"
                make -C "$powsybl_builddir" || exit $?
                export LD_LIBRARY_PATH=$powsybl_builddir/lib:$LD_LIBRARY_PATH
            fi

            if [ $powsybl_docs = true ]; then
                echo "**** Generating Doxygen documentation"
                make -C "$powsybl_builddir" doc || exit $?
            fi
        fi
    fi
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
            mvn -f "$sourceDir/pom.xml" $mvn_options || exit $?
        fi

        if [ $powsybl_docs = true ]; then
            echo "**** Generating Javadoc documentation"
            mvn -f "$sourceDir/pom.xml" javadoc:aggregate || exit $?
            mvn -f "$sourceDir/distribution-core/pom.xml" install || exit $?
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
powsybl_options="prefix:"
thirdparty_options="with-thirdparty,without-thirdparty,thirdparty-prefix:,thirdparty-download,thirdparty-packs:"

opts=`getopt -o '' --long "help,$powsybl_options,$thirdparty_options" -n 'install.sh' -- "$@"`
eval set -- "$opts"
while true; do
    case "$1" in
        # Options
        --prefix) powsybl_prefix=$2 ; shift 2 ;;

        # Third-party options
        --with-thirdparty) thirdparty_build=true ; shift ;;
        --without-thirdparty) thirdparty_build=false ; shift ;;
        --thirdparty-prefix) thirdparty_prefix=$2 ; shift 2 ;;
        --thirdparty-download) thirdparty_download=true ; shift ;;
        --thirdparty-packs) thirdparty_packs=$2 ; thirdparty_download=false ; shift 2 ;;

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
            clean-thirdparty) thirdparty_clean=true ; thirdparty_build=true ;;
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

# Build required C++ thirdparty libraries
buildthirdparty

# Build C++ modules
powsybl_cpp

# Build Java modules
powsybl_java

# Install
powsybl_install

# Save settings
writeSettings > "${settings}"

