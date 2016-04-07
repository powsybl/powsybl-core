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

pidFile=$HOME/.itesla_offline_pid

usage() {
   echo "`basename $0` {start|stop|restart|status}"
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
        echo "offline service already started ($pid)"
    else
        echo "starting offline service"
        logsDir=$installDir/logs
        mkdir $logsDir >> /dev/null 2>&1
        nohup mpirun -n $mpi_tasks -map-by node -host $mpi_hosts $mpirun_options $installBinDir/offline-mpi-task.sh "ui" > $logsDir/offline.log 2>&1&
        pid=$!
        [ $? -eq 0 ] && echo $pid > $pidFile
        sleep 3
        head -4 $logsDir/offline.log
    fi
}

stopKill() {
    readPid
    if [ -n "$pid" ]; then
        echo "killing offline service"
        kill -9 $pid
        rm -f $pidFile
    fi
}

stop() {
    readPid
    if [ -n "$pid" ]; then
        echo "stopping offline service"
        $installBinDir/itools stop-offline-application
        rm -f $pidFile
    fi
}

status() {
    readPid
    if [ -n "$pid" ]; then
        echo "offline service is running ($pid)"
    else
        echo "offline service is not running"
    fi
}

case "$1" in
"start")
    start
    ;;
"stop")
    stop
    ;;
"kill")
    stopKill
    ;;
"restart")
    stop
    start
    ;;
"status")
    status
    ;;
*)
    usage
    ;;
esac


