#!/bin/bash

# Copyright (c) 2016, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

sourceDir=$(dirname $(readlink -f $0))


## install default settings
###############################################################################
ipst_prefix=$HOME/itesla
ipst_package_version=`mvn -U -f "$sourceDir/pom.xml" org.apache.maven.plugins:maven-help-plugin:evaluate -Dexpression=project.version | grep -v "Download" | grep -v "\["`
ipst_package_name=ipst-core-$ipst_package_version
ipst_package_type=zip

thirdparty_build=true
thirdparty_prefix=$HOME/itesla_thirdparty
thirdparty_download=true
thirdparty_packs=$HOME/itesla_packs

# Targets
ipst_clean=false
ipst_compile=false
ipst_docs=false
ipst_package=false
ipst_install=false
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
    echo "  clean                    Clean iPST modules"
    echo "  clean-thirdparty         Clean the thirdparty libraries"
    echo "  compile                  Compile iPST modules"
    echo "  package                  Compile iPST modules and create a distributable package"
    echo "  install                  Compile iPST modules and install it (default target)"
    echo "  help                     Display this help"
    echo "  docs                     Generate the documentation (Doxygen/Javadoc)"
    echo ""
    echo "iPST options:"
    echo "  --help                   Display this help"
    echo "  --prefix                 Set the installation directory (default is $HOME/itesla)"
    echo "  --package-type           Set the package format. The supported formats are zip, tar, tar.gz and tar.bz2 (default is zip)"
    echo ""
    echo "Thirdparty options:"
    echo "  --with-thirdparty        Enable the compilation of thirdparty libraries (default)"
    echo "  --without-thirdparty     Disable the compilation of thirdparty libraries"
    echo "  --thirdparty-prefix      Set the thirdparty installation directory (default is $HOME/itesla_thirdparty)"
    echo "  --thirdparty-download    Sets false to compile thirdparty libraries from a local repository (default is true)"
    echo "  --thirdparty-packs       Sets the thirdparty libraries local repository (default is $HOME/itesla_packs)"
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
    writeComment " -- iPST global options --"
    writeSetting "ipst_prefix" ${ipst_prefix}
    writeSetting "ipst_package_type" ${ipst_package_type}

    writeEmptyLine

    writeComment " -- iPST thirdparty libraries --"
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
    if [[ $thirdparty_clean = true || $ipst_compile = true ]]; then
        echo "** C++ thirdparty libraries"

        if [ $thirdparty_clean = true ]; then
            echo "**** Removing thirdparty install directory (if already exists)."
            rm -rf "$thirdparty_prefix"
        fi

        if [ $ipst_compile = true ]; then
            if [ $thirdparty_build = true ]; then
                 echo "**** Building the C++ thirdparty libraries"
                 thirdparty_builddir="${thirdparty_prefix}/build/ipst-core"
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
ipst_cpp()
{
    if [[ $ipst_clean = true || $ipst_compile = true || $ipst_docs = true ]]; then
        echo "** iPST C++ modules"

        ipst_builddir=$sourceDir/build

        if [ $ipst_clean = true ]; then
            echo "**** Removing build directory (if already exists)."
            rm -rf "$ipst_builddir"
        fi

        if [[ $ipst_compile = true || $ipst_docs = true ]]; then
            # TODO: rename variable
            cmake -DCMAKE_INSTALL_PREFIX="$ipst_prefix" -Dthirdparty_prefix="$thirdparty_prefix" -G "Unix Makefiles" -H"$sourceDir" -B"$ipst_builddir" || exit $?

            if [ $ipst_compile = true ]; then
                echo "**** Compiling C++ modules"
                make -C "$ipst_builddir" || exit $?
                export LD_LIBRARY_PATH=$ipst_builddir/lib:$LD_LIBRARY_PATH
            fi

            if [ $ipst_docs = true ]; then
                echo "**** Generating Doxygen documentation"
                make -C "$ipst_builddir" doc || exit $?
            fi
        fi
    fi
}

## Build Java Modules
###############################################################################
ipst_java()
{
    if [[ $ipst_clean = true || $ipst_compile = true || $ipst_docs = true ]]; then
        echo "** Building iPST Java modules"

        mvn_options=""
        [ $ipst_clean = true ] && mvn_options="$mvn_options clean"
        [ $ipst_compile = true ] && mvn_options="$mvn_options install"
        if [ ! -z "$mvn_options" ]; then
            mvn -f "$sourceDir/pom.xml" $mvn_options || exit $?
        fi

        if [ $ipst_docs = true ]; then
            echo "**** Generating Javadoc documentation"
            mvn -f "$sourceDir/pom.xml" javadoc:javadoc || exit $?
            mvn -f "$sourceDir/distribution-core/pom.xml" install || exit $?
        fi
    fi
}

## Package iPST
###############################################################################
ipst_package()
{
    if [ $ipst_package = true ]; then
        echo "** Packaging iPST"

        case "$ipst_package_type" in
            zip)
                [ -f "${ipst_package_name}.zip" ] && rm -f "${ipst_package_name}.zip"
                $(cd "$sourceDir/distribution-core/target/distribution-core-${ipst_package_version}-full" && zip -rq "$sourceDir/${ipst_package_name}.zip" "itesla")
                zip -qT "${ipst_package_name}.zip" > /dev/null 2>&1 || exit $?
                ;;

            tar)
                [ -f "${ipst_package_name}.tar" ] && rm -f "${ipst_package_name}.tar"
                tar -cf "${ipst_package_name}.tar" -C "$sourceDir/distribution-core/target/distribution-core-${ipst_package_version}-full" . || exit $?
                ;;

            tar.gz | tgz)
                [ -f "${ipst_package_name}.tar.gz" ] && rm -f "${ipst_package_name}.tar.gz"
                [ -f "${ipst_package_name}.tgz" ] && rm -f "${ipst_package_name}.tgz"
                tar -czf "${ipst_package_name}.tar.gz" -C "$sourceDir/distribution-core/target/distribution-core-${ipst_package_version}-full" . || exit $?
                ;;

            tar.bz2 | tbz)
                [ -f "${ipst_package_name}.tar.bz2" ] && rm -f "${ipst_package_name}.tar.bz2"
                [ -f "${ipst_package_name}.tbz" ] && rm -f "${ipst_package_name}.tbz"
                tar -cjf "${ipst_package_name}.tar.bz2" -C "$sourceDir/distribution-core/target/distribution-core-${ipst_package_version}-full" . || exit $?
                ;;

            *)
                echo "Invalid package format: zip, tar, tar.gz, tar.bz2 are supported."
                exit 1;
                ;;
        esac
    fi
}

## Install iPST
###############################################################################
ipst_install()
{
    if [ $ipst_install = true ]; then
        echo "** Installing iPST"

        echo "**** Copying files"
        mkdir -p "$ipst_prefix" || exit $?
        cp -Rp "$sourceDir/distribution-core/target/distribution-core-${ipst_package_version}-full/itesla"/* "$ipst_prefix" || exit $?

        if [ ! -f "$ipst_prefix/etc/itesla.conf" ]; then
            echo "**** Copying configuration files"
            mkdir -p "$ipst_prefix/etc" || exit $?

            echo "#itesla_cache_dir=" >> "$ipst_prefix/etc/itesla.conf"
            echo "#itesla_config_dir=" >> "$ipst_prefix/etc/itesla.conf"
            echo "itesla_config_name=config" >> "$ipst_prefix/etc/itesla.conf"
            echo "#java_xmx=8G" >> "$ipst_prefix/etc/itesla.conf"
            echo "mpi_tasks=3" >> "$ipst_prefix/etc/itesla.conf"
            echo "mpi_hosts=localhost" >> "$ipst_prefix/etc/itesla.conf"
        fi
    fi
}

## Parse command line
###############################################################################
ipst_options="prefix:,package-type:"
thirdparty_options="with-thirdparty,without-thirdparty,thirdparty-prefix:,thirdparty-download,thirdparty-packs:"

opts=`getopt -o '' --long "help,$ipst_options,$thirdparty_options" -n 'install.sh' -- "$@"`
eval set -- "$opts"
while true; do
    case "$1" in
        # iPST options
        --prefix) ipst_prefix=$2 ; shift 2 ;;
        --package-type) ipst_package_type=$2 ; shift 2 ;;

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
            clean) ipst_clean=true ;;
            clean-thirdparty) thirdparty_clean=true ; thirdparty_build=true ;;
            compile) ipst_compile=true ;;
            docs) ipst_docs=true ;;
            package) ipst_package=true ; ipst_compile=true ;;
            install) ipst_install=true ; ipst_compile=true ;;
            help) usage; exit 0 ;;
            *) usage ; exit 1 ;;
        esac
    done
else
    ipst_compile=true
    ipst_install=true
fi

## Build iPST platform
###############################################################################

# Build required C++ thirdparty libraries
buildthirdparty

# Build C++ modules
ipst_cpp

# Build Java modules
ipst_java

# Package iPST
ipst_package

# Install iPST
ipst_install

# Save settings
writeSettings > "${settings}"

