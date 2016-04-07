#!/bin/bash
#
# Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
#


# This script must be called from directory:
# $ITESLA_HOME/platform/modelica-export
#To convert only one system use the class EXECCLASS="eu.itesla_project.modelica_export.test.ModelicaExporterTestLF"
#To convert the N44 systems in the Giuseppe repository use the class EXECCLASS="eu.itesla_project.modelica_export.test.eu.itesla_project.modelica_export.test.N44ConverterTest"

#Nordic 44 in Giuseppe repo
FILEINPUT="/home/machados/sources/N44_WP7/N44_CIM14_snapshots/"
SLACK="_f17695ec_9aeb_11e5_91da_b8763fd99c5f"
EXECCLASS="eu.itesla_project.modelica_export.test.N44ConverterTest"

MODELICAVERSION=3.2

mvn exec:exec -DexecClass=$EXECCLASS -DcimFile=$FILEINPUT -Dhost=127.0.0.1 -DmodelicaVersion=$MODELICAVERSION -DslackAt=$SLACK

