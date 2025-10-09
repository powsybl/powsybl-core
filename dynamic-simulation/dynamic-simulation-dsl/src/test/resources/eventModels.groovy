/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

import com.powsybl.iidm.network.Line
import org.slf4j.Logger
import org.slf4j.LoggerFactory

Logger logger = LoggerFactory.getLogger("com.powsybl.dynamicsimulation.groovy.GroovyEventModelSupplier")

for (Line line : network.lines) {
    dummyEventModel {
        id line.id
        startTime 50
    }
}
