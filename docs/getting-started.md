# Getting started

## "How to run Powsybl without compiling anything" scenario

In this scenario compilation is not necessary, all the jars for a specific Powsybl release will be downloaded from maven central and will be packed into a distribution archive file.

### Requirements:

  * JDK >= *(1.8)*
  * [Maven](https://maven.apache.org/download.cgi) (>= 3.5.3) 

JDK and maven tools commands are expected to be available in the PATH 


### Package a Powsybl distribution
Download this [script](package_powsybl_distribution.sh) 

```bash
#!/bin/bash

# Copyright (c) 2018, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

if [ $# -ne 1 ]; then 
    echo "parameter expected: specify a Powsybl release tag (e.g. v2.0.0)"
    exit -1
fi

##
## POWSYBL_RELEASE holds the release tag ID (>= v2.0.0)
##
POWSYBL_RELEASE=$1

##
## BE CAREFUL: $BUILD_DIR/$POWSYBL_RELEASE will be removed at the end of this packaging process.
##
BUILD_DIR=/tmp/powsybl

wget -xnH  --cut-dirs=2 --no-check-certificate "https://raw.githubusercontent.com/powsybl/powsybl-core/$POWSYBL_RELEASE/pom.xml" "https://raw.githubusercontent.com/powsybl/powsybl-core/$POWSYBL_RELEASE/distribution-core/pom.xml" -P $BUILD_DIR && pushd ./ && cd $BUILD_DIR/$POWSYBL_RELEASE/distribution-core && mvn -Dcheckstyle.skip clean install && popd  && cp "$BUILD_DIR/$POWSYBL_RELEASE/distribution-core/target/powsybl.zip" "./powsybl-$POWSYBL_RELEASE.zip" && rm -rf "$BUILD_DIR/$POWSYBL_RELEASE"
```

and execute it. As its only parameter, it takes the Powsybl version to be packaged (must be > v2.0.0).
You might need to configure your network proxy settings, as the script downloads files from GitHub and maven central. 

After the execution is completed, you can find the Powsybl distribution packed as powsybl-VERSION.zip.
Here we are assuming a v2.0.0 distribution, therefore the archive's name is going to be powsybl-v2.0.0.zip

Please note that the distribution

 * contains just the java portion of the Powsybl framework. To get the C/C++ modules (i.e. the openMPI support libraries) you must go through the complete compile based compilation (ref. ../README.md)
 * does not include any configuration file (ref [configuration](configuration/README.md))

### Run Powsybl example
Unzip powsybl-v2.0.0.zip to a TARGET directory and start a terminal in TARGET/powsybl/bin directory.  

This command converts a network model, encoded in one of supported file formats, to the XIIDM format.

```bash
$> ./itools convert-network --input-file NETWORKFILE  --output-format XIIDM --output-file ./network.xiidm 
```
 
