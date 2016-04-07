# Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
#!/bin/sh

####################################
##  COMPUTE WP43 SECURITY INDEXES
####################################

indexes="overload,underovervoltage,smallsignal,transient";


######################################
#   write MAT files
######################################
write_mat_files() {
    start_time=`date +%s`
    echo "- Executing: wp43adapter $wp43parlist";
    wp43adapter ${wp43parlist}
    cr=$?
    if [[ $cr != 0 ]] ; then
    exit $cr
    fi
    end_time=`date +%s`
    echo wp43adapter execution time was `expr $end_time - $start_time` s.
    echo ""
}

#######################################
#   execute MATLAB compiled indexers
#######################################
execute_matlab_indexer() {
    echo "- Executing:  $matlabindexer ${prefixFileName}_$index.mat ${prefixFileName}_$index${postfixFileName}"
    start_time=`date +%s`
    eval "\"$matlabindexer\"" ${prefixFileName}_$index.mat ${prefixFileName}_wp43_$index${postfixFileName}
    cr=$?
    end_time=`date +%s`
    echo "- "$matlabindexer execution time was `expr $end_time - $start_time` s.
    echo ""
    if [[ $cr != 0 ]] ; then
    exit $cr
    fi
}

cmdUsage() {
    echo "usage: $cmd <caseFolder> <prefixFileName> <configFileParam>";
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

cmd=$0
args=($*)
args=("${args[@]:0}")
[ ${#args[@]} -ne 3 ] && cmdUsage

if [ -z "$indexes" ]; then
    echo "wp43 indexes list is empty";
    exit 0;
fi

caseFolder=${args[0]}
prefixFileName=${args[1]}
configFileParam=${args[2]}
postfixFileName="_security_indexes.xml"
wp43parlist="";
for index in ${indexes//,/ }; do
    wp43parlist+=" -a $index";
done
wp43parlist+=" -o ${prefixFileName} -f ${caseFolder} -n ${prefixFileName} -c ${configFileParam}"

# execute wp43adapter to write .mat files, input to the indexers
write_mat_files

# execute the indexers
for index in ${indexes//,/ }; do
case $index in
    "overload") matlabindexer="wp43_overload"; execute_matlab_indexer;;
    "underovervoltage") matlabindexer="wp43_underovervoltage"; execute_matlab_indexer;;
    "smallsignal") matlabindexer="wp43_smallsignal"; execute_matlab_indexer;;
    "transient") matlabindexer="wp43_transient"; execute_matlab_indexer;;
    *) echo $index " not supported";;
esac
done


