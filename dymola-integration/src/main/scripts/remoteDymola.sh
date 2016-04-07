#!/bin/bash
#
# Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
#


execute_remote_dymola() {
    echo "Executing remote Dymola simulation:"

    if [ $dumpParametersValues = 1 ]; then
     echo " modelFileName:" $modelFileName
     echo " modelName:" $modelName
     echo " workingDir:" $workingDir
     echo " cId:" $cId
     echo " wsdlService:" $wsdlService
     echo " startTime:" $startTime
     echo " stopTime:" $stopTime
     echo " numberOfIntervals:" $numberOfIntervals
     echo " outputInterval:" $outputInterval
     echo " method:" $method
     echo " tolerance:" $tolerance
     echo " outputFixedstepSize:" $outputFixedstepSize
     echo " inputZipFileName:" $inputZipFileName
     echo " outputZipFileName:" $outputZipFileName
     echo " outputDymolaMatFileName:" $outputDymolaMatFileName
     echo " outputErrorsFileName:" $outputErrorsFileName
     echo " isFake:" $isFake
    fi

    start_time=`date +%s`
    $JAVA_HOME/bin/java -cp "$installDir/share/java/*" -Dlogback.configurationFile=$installDir/share/java/logback-cmdline.xml eu.itesla_project.dymola.StandaloneDymolaClient $modelFileName $modelName $workingDir $cId $wsdlService $startTime $stopTime $numberOfIntervals $outputInterval $method $tolerance $outputFixedstepSize $inputZipFileName $outputZipFileName $outputDymolaMatFileName $outputErrorsFileName $isFake
    cr=$?
    if [[ $cr != 0 ]] ; then
	exit $cr
    fi
    end_time=`date +%s`
    echo remote Dymola execution time was `expr $end_time - $start_time` s.
    echo ""
    #unzip results
    unzip dymolaoutput_$cId
    cr=$?
    if [[ $cr != 0 ]] ; then
    exit $cr
    fi
}


installBinDir=$(dirname $(readlink -f $0))
#installBinDir=/home/itesla/itesla_dymola/bin
installDir=${installBinDir%/*}
export PATH=$installBinDir:$PATH
dumpParametersValues=1

modelFileName=${1}
modelName=${2}
workingDir=${3}
cId=${4}
wsdlService=${5}
startTime=${6}
stopTime=${7}
numberOfIntervals=${8}
outputInterval=${9}
method=${10}
tolerance=${11}
outputFixedstepSize=${12}
inputZipFileName=${13}
outputZipFileName=${14}
outputDymolaMatFileName=${15}
outputErrorsFileName=${16}
isFake=${17}

execute_remote_dymola