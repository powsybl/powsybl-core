#!/bin/sh
#
# Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
#

###################################################
##  COMPUTE WP43 SECURITY INDEXES (DYMOLA VERSION)
###################################################

#indexes="overload,underovervoltage,smallsignal,transient";

###########################################################
#   extract data from dymola output, to feed WP43 indexers
###########################################################
write_indexer_inputs() {
    start_time=`date +%s`
    echo "- Executing: $matlabadapter ${prefixFileName}.mat ${prefixFileName}_wp43_$index.mat ${prefixFileName}_wp43_${index}_pars.mat"
    eval "\"$matlabadapter\"" ${prefixFileName}.mat ${prefixFileName}_wp43_$index.mat ${prefixFileName}_wp43_${index}_pars.mat
    cr=$?
    if [[ $cr != 0 ]] ; then
    exit $cr
    fi
    end_time=`date +%s`
    echo $matlabadapter execution time was `expr $end_time - $start_time` s.
    echo ""
}

#######################################
#   execute MATLAB compiled indexers
#######################################
execute_matlab_indexer() {
    echo "- Executing:  $matlabindexer ${prefixFileName}_wp43_$index.mat ${prefixFileName}_$index${postfixFileName}"
    start_time=`date +%s`
    eval "\"$matlabindexer\"" ${prefixFileName}_wp43_$index.mat ${prefixFileName}_wp43_$index${postfixFileName}
    cr=$?
    end_time=`date +%s`
    echo "- "$matlabindexer execution time was `expr $end_time - $start_time` s.
    echo ""
    if [[ $cr != 0 ]] ; then
    exit $cr
    fi
}

cmdUsage() {
    echo "usage: $cmd <caseFolder> <prefixFileName> <indexesNamesList>";
    exit
}

cleanup() {
    #test -n "$tmpdir" && test -d "$tmpdir" && rm -rf "$tmpdir"
    echo "";
}


if [ -z "$MCRROOT" ]; then
    echo "Need to set enviroment variable MCRROOT (points to Mmatlab runtime home directory)."
    exit 1
fi

LD_LIBRARY_PATH=.:${MCRROOT}/runtime/glnxa64 ;
LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:${MCRROOT}/bin/glnxa64 ;
LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:${MCRROOT}/sys/os/glnxa64;
export LD_LIBRARY_PATH;

#tmpdir=$(mktemp -d)
trap cleanup EXIT
trap 'cleanup; exit 127' INT TERM

echo "--------------------";
echo "COMPUTE WP43 INDEXES";
echo "--------------------";

cmd=$0
args=($*)
args=("${args[@]:0}")
[ ${#args[@]} -ne 3 ] && cmdUsage

caseFolder=${args[0]}
prefixFileName=${args[1]}
postfixFileName="_security_indexes.xml"
indexes=${args[2]}
echo "caseFolder:  $caseFolder";
echo "prefixFileName:  $prefixFileName";
echo "indexes:  $indexes";
echo "";
if [ -z "$indexes" ]; then
    echo "wp43 indexes list is empty";
    exit 0;
fi


# execute the indexers
for index in ${indexes//,/ }; do
case $index in
    "overload") matlabadapter="wp43dymadapter_overload"; matlabindexer="wp43_overload";  write_indexer_inputs; execute_matlab_indexer;;
    "underovervoltage") matlabadapter="wp43dymadapter_underovervoltage"; matlabindexer="wp43_underovervoltage"; write_indexer_inputs; execute_matlab_indexer;;
    "smallsignal") matlabadapter="wp43dymadapter_smallsignal"; matlabindexer="wp43_smallsignal"; write_indexer_inputs; execute_matlab_indexer;;
    "transient") matlabadapter="wp43dymadapter_transient"; matlabindexer="wp43_transient"; write_indexer_inputs; execute_matlab_indexer;;
    *) echo $index " not supported";;
esac
done


