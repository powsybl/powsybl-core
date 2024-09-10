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

    private static final int MAX_BUS_LENGTH = 12;

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

    static void createBuses(PssePowerFlowModel psseModel, ContextExport contextExport) {

        contextExport.getLinkExport().getBusISet().forEach(busI -> {
            // bus and type is always present when a psseBus is created
            Bus busViewBus = contextExport.getLinkExport().getBusView(busI).orElseThrow();
            int type = findBusViewBusType(busViewBus.getVoltageLevel(), busViewBus);
            psseModel.addBuses(Collections.singletonList(createNewBus(busViewBus, busI, type)));
        });
        psseModel.replaceAllBuses(psseModel.getBuses().stream().sorted(Comparator.comparingInt(PsseBus::getI)).toList());
    }

    private static PsseBus createNewBus(Bus bus, int busI, int type) {
        PsseBus psseBus = new PsseBus();
        psseBus.setI(busI);
        psseBus.setName(fixBusName(bus.getNameOrId()));
        psseBus.setBaskv(bus.getVoltageLevel().getNominalV());
        psseBus.setIde(type);
        psseBus.setArea(1);
        psseBus.setZone(1);
        psseBus.setOwner(1);
        psseBus.setVm(getVm(bus));
        psseBus.setVa(getVa(bus));
        psseBus.setNvhi(getHighVm(bus));
        psseBus.setNvlo(getLowVm(bus));
        psseBus.setEvhi(getHighVm(bus));
        psseBus.setEvlo(getLowVm(bus));

        return psseBus;
    }

    // first character must not be a minus sign
    private static String fixBusName(String name) {
        String fixedName = name.startsWith("-") ? "_" + name.substring(1) : name;
        return fixedName.length() > MAX_BUS_LENGTH ? fixedName.substring(0, MAX_BUS_LENGTH) : fixedName;
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
