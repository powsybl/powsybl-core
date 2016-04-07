#!/bin/bash
#
# Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
#

FPF_BINARY_JAR=jClassicFuzzyPowerFlow.jar

cmdUsage() {
    echo "usage: $cmd <outputDir> <inputFile>";
    exit
}
        

cmd=$0
args=($*)
args=("${args[@]:0}")
[ ${#args[@]} -ne 2 ] && cmdUsage

workingDir=$1
inputFileName=$2

echo "Executing $FPF_BINARY_JAR"
start_time=`date +%s`

mkdir -p ${workingDir} && cd $workingDir && java -jar $FPFHOME/$FPF_BINARY_JAR  $inputFileName
cr=$?
end_time=`date +%s`
echo ""
#echo "- "$FPF_BINARY_JAR execution time was `expr $end_time - $start_time` s.
#echo ""
if [[ $cr != 0 ]] ; then
 exit $cr
fi
echo ""
echo "zipping output files:"
zip fpfclassic_output.zip Run*.txt
cr=$?
if [[ $cr != 0 ]] ; then
 exit $cr
fi
