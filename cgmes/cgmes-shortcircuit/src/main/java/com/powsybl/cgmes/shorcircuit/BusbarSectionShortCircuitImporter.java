/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.shorcircuit;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuit;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuitAdder;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class BusbarSectionShortCircuitImporter {

    private final Network network;

    BusbarSectionShortCircuitImporter(Network network) {
        this.network = Objects.requireNonNull(network);
    }

    void importData(PropertyBag busbarSectionPropertyBag) {
        Objects.requireNonNull(busbarSectionPropertyBag);

        // NodeBreaker models
        String busbarSectionId = busbarSectionPropertyBag.getId("BusbarSection");
        BusbarSection busbarSection = network.getBusbarSection(busbarSectionId);

        if (busbarSection != null) {
            Double ipMax = busbarSectionPropertyBag.asDouble("ipMax");
            if (!Double.isNaN(ipMax)) {
                busbarSection.newExtension(IdentifiableShortCircuitAdder.class)
                    .withIpMax(ipMax)
                    .add();
            }
            return;
        }

        // BusBranch models
        Bus bus = getBus(network, busbarSectionPropertyBag, busbarSectionId);
        if (bus == null) {
            return;
        }
        Double ipMax = busbarSectionPropertyBag.asDouble("ipMax");
        if (!Double.isNaN(ipMax)) {
            setMimimunIpMaxToBus(bus, ipMax);
        }
    }

    private static Bus getBus(Network network, PropertyBag busbarSectionPropertyBag, String busbarSectionId) {
        String topologicalNodeId = busbarSectionPropertyBag.getId("TopologicalNode");
        if (topologicalNodeId == null) {
            LOG.warn("BusbarSection {}. Associated topologicalNode not found", busbarSectionId);
            return null;
        }
        Bus bus = network.getBusBreakerView().getBus(topologicalNodeId);
        if (bus == null) {
            LOG.warn("BusbarSection {}. Associated bus not found", busbarSectionId);
        }
        return bus;
    }

    private static void setMimimunIpMaxToBus(Bus bus, double ipMax) {
        IdentifiableShortCircuit<Bus> busShortCircuit = bus.getExtension(IdentifiableShortCircuit.class);
        if (busShortCircuit == null) {
            bus.newExtension(IdentifiableShortCircuitAdder.class)
                .withIpMax(ipMax)
                .add();
        } else {
            if (ipMax < busShortCircuit.getIpMax()) {
                busShortCircuit.setIpMax(ipMax);
            }
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(BusbarSectionShortCircuitImporter.class);
}

