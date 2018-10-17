

# Powsybl-Core

[![Build Status](https://travis-ci.org/powsybl/powsybl-core.svg?branch=master)](https://travis-ci.org/powsybl/powsybl-core)
[![Build status](https://ci.appveyor.com/api/projects/status/76o2bbmsewpbpr97/branch/master?svg=true)](https://ci.appveyor.com/project/powsybl/powsybl-core/branch/master)
[![Coverage Status](https://coveralls.io/repos/github/powsybl/powsybl-core/badge.svg?branch=master)](https://coveralls.io/github/powsybl/powsybl-core?branch=master)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=com.powsybl%3Apowsybl-core&metric=coverage)](https://sonarcloud.io/component_measures?id=com.powsybl%3Apowsybl-core&metric=coverage)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=com.powsybl%3Apowsybl-core&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.powsybl%3Apowsybl-core)
[![MPL-2.0 License](https://img.shields.io/badge/license-MPL_2.0-blue.svg)](https://www.mozilla.org/en-US/MPL/2.0/)

http://www.powsybl.com

## Overview
Powsybl (Power System Blocks) is an open-source Java framework dedicated to building power system oriented software. 
Its source code is distributed under the Mozilla Public License 2.0 and it is hosted on GitHub: https://github.com/powsybl .
  
The project powsybl-core provides the framework's core functionalities. It is designed in a modular approach, 
to allow other projects to extend its features or to modify the default behaviours.

* IIDM (iTesla Internal Data Model) APIs: able to represent power grid models (substations, voltage levels, lines, two and three windings transformers, generator, etc.)
* Data converters APIs: currently, to load from Entso-E CIM/CGMES, UCTE, AMPL, XIIDM XML and save to XIIDM XML, AMPL 
* Contingencies APIs
* iAL iTesla Action Language: a domain specific language for action simulation on the network
* Load-Flow & Simulation APIs
* Computation modules, including distribution functionalities based on MPI (Message Passing Interface)
* Standard Security Analysis APIs
* AFS (Application File System)
* Scripting (Groovy)
* Tools: command line tools interface

## The Powsybl Project

[Powsybl](powsybl.com) is part of the [LF Energy Foundation](lfenergy.org), a project of [The Linux Foundation](linuxfoundation.org)
that supports open source innovation projects within the energy and electricity sectors.

## Learn more about Powsybl-Core

* Our [User's Guide](docs/installation/install-from-sources.md) to build a binary distribution from the project sources
* Our [User's Guide](docs/installation/install-from-maven.md) to build a binary distribution from pre-compiled binaries from a Maven repository
* Our [Documentation](docs/README.md) for more information

## Releases

See our [releases](https://github.com/powsybl/powsybl-core/releases).

## How to Contribute

Contributors are always welcome!
If you have a bug or an idea, please read our [documentation](docs/README.md) before opening an issue.
Issues labelled [good first issue](https://github.com/powsybl/powsybl-core/labels/good%20first%20issue) can be good first contributions.


<!-- MRA: This README should only present the project, I moved the installation guide in docs/installation along with the former Getting-Started -->
