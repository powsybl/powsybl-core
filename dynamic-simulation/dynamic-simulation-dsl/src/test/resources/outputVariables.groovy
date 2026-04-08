/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */


import com.powsybl.iidm.network.Generator
import com.powsybl.iidm.network.Load
import org.slf4j.Logger
import org.slf4j.LoggerFactory

Logger logger = LoggerFactory.getLogger("com.powsybl.dynamicsimulation.groovy.GroovyOutputVariablesSupplier")

dummyCurve {
    id "id"
    variable "variable"
}

dummyFsv {
    id "id"
    variable "variable"
}

for (Load load : network.loads) {
    dummyCurve {
        id load.id
        variable "p0"
    }
}

for (Generator generator : network.generators) {
    if (generator.terminal.voltageLevel.nominalV < 400) {
        logger.info("Skip generator: " + generator.id)
        continue
    }
    dummyCurve {
        id generator.id
        variable "p"
    }
}
