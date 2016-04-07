#!/bin/bash
#
# Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
#


# This script must be called from directory:
# $ITESLA_HOME/platform/modelica-events-adder

EXECCLASS="eu.itesla_project.modelica_events_adder.events.test.ModelicaExportEventsTest"
MODELICAFILE="/home/machados/sources/itesla/platform/modelica-export/PT_20091216_1130.mo"
EVENTSFILE="/home/machados/sources/itesla/platform/modelica-events-adder/events/CIMEvents_example.csv"

mvn exec:exec -DexecClass=$EXECCLASS -DmodFile=$MODELICAFILE -DeventsFile=$EVENTSFILE