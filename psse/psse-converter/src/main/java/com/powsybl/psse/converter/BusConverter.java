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

    static void updateBuses(PssePowerFlowModel psseModel, ContextExport contextExport) {
        psseModel.getBuses().forEach(psseBus -> {
            Optional<Bus> busViewBus = contextExport.getLinkExport().getBusView(psseBus.getI());
            if (busViewBus.isPresent()) {
                updatePsseBus(busViewBus.get(), findBusType(busViewBus.get().getVoltageLevel(), busViewBus.get(), psseBus), psseBus);
            } else {
                updateIsolatedPsseBus(psseBus);
            }
        });
    }

    // type is preserved in the update of nodeBreaker topologies. Type is calculated internally by psse based on the status of switches
    private static int findBusType(VoltageLevel voltageLevel, Bus busView, PsseBus psseBus) {
        return exportVoltageLevelAsNodeBreaker(voltageLevel) ? psseBus.getIde() : findBusViewBusType(voltageLevel, busView);
    }

    private static void updatePsseBus(Bus busView, int type, PsseBus psseBus) {
        if (busView == null) {
            updateIsolatedPsseBus(psseBus);
        } else {
            if (type == 4) {
                updateIsolatedPsseBus(psseBus);
            } else {
                psseBus.setVm(getVm(busView));
                psseBus.setVa(getVa(busView));
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
