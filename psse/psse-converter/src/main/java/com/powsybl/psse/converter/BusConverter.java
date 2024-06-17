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
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.pf.PsseBus;
import com.powsybl.psse.model.pf.PssePowerFlowModel;
import org.apache.commons.lang3.StringUtils;

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

    static void updateAndCreateBuses(Network network, PssePowerFlowModel psseModel, ContextExport contextExport) {
        Map<Integer, PsseBus> busNumToPsseBus = new HashMap<>();
        psseModel.getBuses().forEach(psseBus -> busNumToPsseBus.put(psseBus.getI(), psseBus));

        contextExport.getBusBreakerExport().getBuses().forEach(busViewBusId -> {
            int busI = contextExport.getBusBreakerExport().getBusBusI(busViewBusId).orElseThrow();
            int type = contextExport.getBusBreakerExport().getBusType(busViewBusId).orElseThrow();
            updateAndCreateBus(network, busViewBusId, busI, type, null, psseModel, busNumToPsseBus);
        });

        contextExport.getNodeBreakerExport().getBuses().forEach(busViewBusId -> {
            int busI = contextExport.getNodeBreakerExport().getBusBusI(busViewBusId).orElseThrow();
            Integer busCopy = contextExport.getNodeBreakerExport().getBusBusCopy(busViewBusId).orElse(null);
            int type = contextExport.getNodeBreakerExport().getBusType(busViewBusId).orElseThrow();
            updateAndCreateBus(network, busViewBusId, busI, type, busCopy, psseModel, busNumToPsseBus);
        });

        contextExport.getNodeBreakerExport().getIsolatedBusesI().forEach(busI -> {
            VoltageLevel voltageLevel = contextExport.getNodeBreakerExport().getIsolatedBusIVoltageLevel(busI).orElseThrow();
            Integer busCopy = contextExport.getNodeBreakerExport().getIsolatedBusIBusCopy(busI).orElse(null);
            createIsolatedBus(voltageLevel, busI, busCopy, psseModel, busNumToPsseBus);
        });

        psseModel.replaceAllBuses(psseModel.getBuses().stream().sorted(Comparator.comparingInt(PsseBus::getI)).toList());
    }

    private static void updateAndCreateBus(Network network, String busViewBusId, int busI, int type, Integer busCopy, PssePowerFlowModel psseModel, Map<Integer, PsseBus> busNumToPsseBus) {
        if (busNumToPsseBus.containsKey(busI)) {
            PsseBus psseBus = busNumToPsseBus.get(busI);
            updatePsseBus(network, busViewBusId, type, psseBus);
        } else if (busCopy != null) {
            PsseBus psseBus = createNewBusFromCopy(busCopy, busI, busNumToPsseBus);
            updatePsseBus(network, busViewBusId, type, psseBus);
            psseModel.addBuses(Collections.singletonList(psseBus));
        } else {
            Bus bus = Objects.requireNonNull(network.getBusView().getBus(busViewBusId));
            psseModel.addBuses(Collections.singletonList(createNewBus(bus, busI, type)));
        }
    }

    private static void createIsolatedBus(VoltageLevel voltageLevel, int busI, Integer busCopy, PssePowerFlowModel psseModel, Map<Integer, PsseBus> busNumToPsseBus) {
        if (busNumToPsseBus.containsKey(busI)) {
            PsseBus psseBus = busNumToPsseBus.get(busI);
            updateIsolatedPsseBus(psseBus);
        } else if (busCopy != null) {
            PsseBus psseBus = createNewBusFromCopy(busCopy, busI, busNumToPsseBus);
            updateIsolatedPsseBus(psseBus);
            psseModel.addBuses(Collections.singletonList(psseBus));
        } else {
            psseModel.addBuses(Collections.singletonList(createNewIsolatedBus(voltageLevel, busI)));
        }
    }

    private static void updatePsseBus(Network network, String busViewBusId, int type, PsseBus psseBus) {
        Bus bus = network.getBusView().getBus(busViewBusId);
        if (bus == null) {
            updateIsolatedPsseBus(psseBus);
        } else {
            psseBus.setVm(getVm(bus));
            psseBus.setVa(getVa(bus));
            psseBus.setIde(type);
        }
    }

    private static PsseBus createNewBusFromCopy(int copyBus, int newBus, Map<Integer, PsseBus> busNumToPsseBus) {
        PsseBus psseBusToBeCopied = findPsseBus(copyBus, busNumToPsseBus);
        PsseBus psseBus = psseBusToBeCopied.copy();
        psseBus.setI(newBus);
        psseBus.setName(findNewBusName(psseBus.getName().trim(), "-" + newBus));
        return psseBus;
    }

    // new bus name is obtained by adding "-" + newBus, but MAX_BUS_LENGTH must be ensured
    private static String findNewBusName(String baseName, String tag) {
        int newLength = baseName.length() + tag.length();
        if (newLength < MAX_BUS_LENGTH) {
            return StringUtils.rightPad(baseName + tag, MAX_BUS_LENGTH, " ");
        } else if (newLength == MAX_BUS_LENGTH) {
            return baseName + tag;
        } else {
            return baseName.substring(0, MAX_BUS_LENGTH - tag.length()) + tag;
        }
    }

    private static PsseBus findPsseBus(int bus, Map<Integer, PsseBus> busNumToPsseBus) {
        if (busNumToPsseBus.containsKey(bus)) {
            return busNumToPsseBus.get(bus);
        } else {
            throw new PsseException("Unexpected null PsseBus for " + bus);
        }
    }

    private static void updateIsolatedPsseBus(PsseBus psseBus) {
        psseBus.setVm(0.0);
        psseBus.setVa(0.0);
        psseBus.setIde(4);
    }

    private static PsseBus createNewBus(Bus bus, int busI, int type) {
        PsseBus psseBus = new PsseBus();
        psseBus.setI(busI);
        psseBus.setName(bus.getNameOrId());
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

    private static PsseBus createNewIsolatedBus(VoltageLevel voltageLevel, int busI) {
        PsseBus psseBus = new PsseBus();
        psseBus.setI(busI);
        psseBus.setName(voltageLevel.getId() + "-" + busI);
        psseBus.setBaskv(voltageLevel.getNominalV());

        updateIsolatedPsseBus(psseBus);

        return psseBus;
    }

    private final PsseBus psseBus;
    private final NodeBreakerImport nodeBreakerImport;
}
