#!/bin/bash
#
# Copyright (c) 2016, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
#

rank=$OMPI_COMM_WORLD_RANK

cmd=$0
usage() {
    # only print usage on master process
    if [ $rank = 0 ]; then
        echo "Usage: $cmd [options] [script]"
        echo ""
        echo "Options:"
        echo "  --help                   Display this help"
        echo "  --verbose                Verbose mode"
        echo ""
    fi
}

verbose=false

opts=`getopt -o '' --long "help,verbose" -n 'groovy-script-mpi-task' -- "$@"`
eval set -- "$opts"
while true; do
    case "$1" in
        --verbose) verbose=true ; shift ;;
        --help) usage ; exit 0 ;;
        --) shift ; break ;;
        *) usage ; exit 1 ;;
    esac
done

if [ -n "$OMPI_MCA_rmaps_base_cpus_per_rank" ]; then
    cores=$OMPI_MCA_rmaps_base_cpus_per_rank
else
    cores=1
fi

tmpDirPrefix=$HOME/tmp
mkdir -p $tmpDirPrefix
tmpDir=`mktemp -d -p $tmpDirPrefix`

args="--tmp-dir=$tmpDir --cores=$cores"
if [ $verbose = true ]; then
    args+=" --verbose"
fi

if [ $rank = 0 ]; then
    export LD_PRELOAD=libmpi.so
    itools mpi-master-groovy-script $args --script $1
else
    slave $args
fi

rm -r $tmpDir

