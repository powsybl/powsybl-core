/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.shorcircuit;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.conversion.CgmesImportPostProcessor;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.TripleStore;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(CgmesImportPostProcessor.class)
public class CgmesShortCircuitPostProcessor implements CgmesImportPostProcessor {

    @Override
    public String getName() {
        return "shortcircuit";
    }

    @Override
    public void process(Network network, TripleStore tripleStore) {
        new CgmesShortCircuitImporter(new CgmesShortCircuitModel(tripleStore), network).importShortcircuitData();
    }
}
