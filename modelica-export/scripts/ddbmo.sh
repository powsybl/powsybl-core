#!/bin/bash
#
# Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
#


# This script must be called from directory:
# $ITESLA_HOME/platform/iidm-ddb/iidm-ddb-modelica-import-export

FILEINPUT="path_to_ipsl/iPSL.mo"
FILETMP="temp_dir_path/temp"
FILEMAPPING="path_to_mapping/PSSE2MOD.txt"

mkdir -pv $FILETMP
mvn -e -X exec:exec -DInputdir=$FILEINPUT -DTempdir=$FILETMP -DMapperdir=$FILEMAPPING -DHost=127.0.0.1 -DPort=8080 -DUser=user -DPassword=password -DIslibrary=true -DIsregulator=false 
