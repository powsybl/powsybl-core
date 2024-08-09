/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import java.util.*;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.model.pf.PsseBus;
import com.powsybl.psse.model.pf.PssePowerFlowModel;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class BusConverter extends AbstractConverter {

    BusConverter(PsseBus psseBus, ContainersMapping containerMapping, Network network, NodeBreakerImport nodeBreakerImport) {
        super(containerMapping, network);
        this.psseBus = Objects.requireNonNull(psseBus);
        this.nodeBreakerImport = Objects.requireNonNull(nodeBreakerImport);
    }

    void create(VoltageLevel voltageLevel) {
        // The bus is only created in busBranch voltage levels, when bus is a connectivity bus.
        if (nodeBreakerImport.isTopologicalBus(psseBus.getI())) {
            return;
        }

        String busId = getBusId(psseBus.getI());
        Bus bus = voltageLevel.getBusBreakerView().newBus()
                .setId(busId)
                .setName(psseBus.getName())
                .add();
        bus.setV(psseBus.getVm() * voltageLevel.getNominalV())
                .setAngle(psseBus.getVa());
    }

    static void updateBuses(Network network, PssePowerFlowModel psseModel, ContextExport contextExport) {
        psseModel.getBuses().forEach(psseBus -> {
            Optional<String> busViewBusId = contextExport.getBusViewBusId(psseBus.getI());
            OptionalInt type = contextExport.getType(psseBus.getI());
            if (busViewBusId.isPresent() && type.isPresent()) {
                updatePsseBus(network, busViewBusId.get(), type.getAsInt(), psseBus);
            } else {
                updateIsolatedPsseBus(psseBus);
            }
        });
    }

    private static void updatePsseBus(Network network, String busViewBusId, int type, PsseBus psseBus) {
        Bus bus = network.getBusView().getBus(busViewBusId);
        if (bus == null) {
            updateIsolatedPsseBus(psseBus);
        } else {
            if (type == 4) {
                updateIsolatedPsseBus(psseBus);
            } else {
                psseBus.setVm(getVm(bus));
                psseBus.setVa(getVa(bus));
                psseBus.setIde(type);
            }
        }
    }

    private static void updateIsolatedPsseBus(PsseBus psseBus) {
        psseBus.setVm(0.0);
        psseBus.setVa(0.0);
        psseBus.setIde(4);
    }

    private final PsseBus psseBus;
    private final NodeBreakerImport nodeBreakerImport;
}
