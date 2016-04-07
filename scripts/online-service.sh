#!/bin/bash
#
# Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
#

installBinDir=$(dirname $(readlink -f $0))
installDir=${installBinDir%/*}

. $installDir/etc/itesla.conf

pidFile=$HOME/.itesla_online_pid

TIME=`date +%Y-%m-%d_%H_%M_%S`

usage() {
   echo "`basename $0` {start|stop|restart|status|clean-cache|clean-tmp}"
}

readPid() {
    pid=""
    if [ -f  $pidFile ]; then
        pid=`cat $pidFile`
        # check pid exists anf if not clean pid file
        kill -0 $pid > /dev/null 2>&1
        if [ $? -ne 0 ]; then
            rm -f $pidFile
        fi
    fi
}

start() {
    readPid
    if [ -n "$pid" ]; then
        echo "online service already started ($pid)"
    else
        echo "starting online service"
        logsDir=$installDir/logs
        mkdir $logsDir >> /dev/null 2>&1
        mv $logsDir/online.log $logsDir/online.log_${TIME} >> /dev/null 2>&1
        nohup mpirun -n $mpi_tasks -map-by node -host $mpi_hosts $mpirun_options $installBinDir/online-mpi-task.sh "ui" > $logsDir/online.log 2>&1&
        pid=$!
        [ $? -eq 0 ] && echo $pid > $pidFile
        sleep 3
        head -4 $logsDir/online.log
    fi
}

stop() {
    readPid
    if [ -n "$pid" ]; then
        echo "stopping online service"
        kill -9 $pid
        rm -f $pidFile
    fi
}

status() {
    readPid
    if [ -n "$pid" ]; then
        echo "online service is running ($pid)"
    else
        echo "online service is not running"
    fi
}

clean_cache() {
    readPid
    if [ -n "$pid" ]; then
        echo "cannot clean cache: online service is running ($pid)"
    else
        if [ -n "$itesla_cache_dir" ]; then
            echo "cleaning cache ( $itesla_cache_dir ) ..."
            rm -r $itesla_cache_dir >> /dev/null 2>&1
            mkdir $itesla_cache_dir >> /dev/null 2>&1
            touch $itesla_cache_dir/dontdeleteme >> /dev/null 2>&1
            echo "cache cleaned."
        else
            echo "variable itesla_cache-dir not defined"
        fi
    fi
}

clean_tmp() {
    readPid
    if [ -n "$pid" ]; then
        echo "cannot clean tmp directory: online service is running ($pid)"
    else
        echo "cleaning tmp directory...."
        rm -r $installDir/tmp >> /dev/null 2>&1
        mkdir $installDir/tmp >> /dev/null 2>&1
        touch $installDir/tmp/dontdeleteme >> /dev/null 2>&1
        echo "tmp directory cleaned."
    fi
}



case "$1" in
"start")
    start
    ;;
"stop")
    stop
    ;;
"restart")
    stop
    start
    ;;
"status")
    status
    ;;
"clean-cache")
    clean_cache
    ;;
"clean-tmp")
    clean_tmp
    ;;
*)
    usage
    ;;
esac


