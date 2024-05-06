package com.powsybl.cgmes.shorcircuit; /**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

import java.util.Objects;

import com.powsybl.iidm.network.Network;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */

public class CgmesShortCircuitImporter {

    private final Network network;
    private final CgmesShortCircuitModel cgmesScModel;

    public CgmesShortCircuitImporter(CgmesShortCircuitModel cgmesScModel, Network network) {
        this.network = Objects.requireNonNull(network);
        this.cgmesScModel = Objects.requireNonNull(cgmesScModel);
    }

    public void importShortcircuitData() {
        importBusBarSections();
        importGenerators();
    }

    private void importBusBarSections() {
        BusbarSectionShortCircuitImporter busbarSectionScImporter = new BusbarSectionShortCircuitImporter(network);
        cgmesScModel.getBusbarSectionsShortcircuitData().forEach(busbarSectionScImporter::importData);
    }

    private void importGenerators() {
        GeneratorShortCircuitImporter generatorImporter = new GeneratorShortCircuitImporter(network);
        cgmesScModel.getSynchronousMachinesShortcircuitData().forEach(generatorImporter::importData);
    }
}

