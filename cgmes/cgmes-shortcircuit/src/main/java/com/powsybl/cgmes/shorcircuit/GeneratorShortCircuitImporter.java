package com.powsybl.cgmes.shorcircuit; /**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

import java.util.Objects;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuitAdder;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class GeneratorShortCircuitImporter {

    private final Network network;

    GeneratorShortCircuitImporter(Network network) {
        this.network = Objects.requireNonNull(network);
    }

    void importData(PropertyBag synchrohousMachine) {
        Objects.requireNonNull(synchrohousMachine);

        String generatorId = synchrohousMachine.getId("SynchronousMachine");
        Generator generator = network.getGenerator(generatorId);
        if (generator == null) {
            return;
        }
        double satDirectSubtransXpu = synchrohousMachine.asDouble("satDirectSubtransX");
        double satDirectTransXpu = synchrohousMachine.asDouble("satDirectTransX");

        double vNominal = generator.getTerminal().getVoltageLevel().getNominalV();
        double satDirectSubtransX = CgmesShortCircuitImporterUtils.impedanceToEngineeringUnit(satDirectSubtransXpu, vNominal, PerUnit.SB);
        double satDirectTransX = CgmesShortCircuitImporterUtils.impedanceToEngineeringUnit(satDirectTransXpu, vNominal, PerUnit.SB);

        if (!Double.isNaN(satDirectSubtransX) || !Double.isNaN(satDirectTransX)) {
            GeneratorShortCircuitAdder adder = generator.newExtension(GeneratorShortCircuitAdder.class);
            if (!Double.isNaN(satDirectSubtransX)) {
                adder.withDirectSubtransX(satDirectSubtransX);
            }
            if (!Double.isNaN(satDirectTransX)) {
                adder.withDirectTransX(satDirectTransX);
            }
            adder.add();
        }
    }
}

