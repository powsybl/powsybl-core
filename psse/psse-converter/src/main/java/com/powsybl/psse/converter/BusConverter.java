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

    // At the moment we only consider new buses created by opening switches in nodeBreaker substations
    static void updateBuses(Network network, PssePowerFlowModel psseModel, NodeBreakerExport nodeBreakerExport) {
        Map<Integer, PsseBus> busNumToPsseBus = new HashMap<>();

        psseModel.getBuses().forEach(psseBus -> {

            String busId = nodeBreakerExport.getBusBreakerBusId(psseBus.getI()).orElseGet(() -> AbstractConverter.getBusId(psseBus.getI()));
            int type = nodeBreakerExport.getBusType(psseBus.getI()).orElseGet(psseBus::getIde);

            updatePsseBus(network, busId, psseBus, type);

            busNumToPsseBus.put(psseBus.getI(), psseBus);
        });

        // create new psse buses
        List<PsseBus> addedBuses = new ArrayList<>();
        nodeBreakerExport.getNewBusesSet().stream().sorted().forEach(newBus -> {
            int copyBus = nodeBreakerExport.getNewBusCopyBus(newBus).orElseThrow();
            String busBreakerId = nodeBreakerExport.getNewBusBusBreakerId(newBus).orElseThrow();
            int type = nodeBreakerExport.getNewBusType(newBus).orElseThrow();

            PsseBus psseBus = createNewBus(copyBus, newBus, busNumToPsseBus);
            updatePsseBus(network, busBreakerId, psseBus, type);

            addedBuses.add(psseBus);
        });

        psseModel.addBuses(addedBuses);
    }

    private static PsseBus createNewBus(int copyBus, int newBus, Map<Integer, PsseBus> busNumToPsseBus) {
        PsseBus psseBusToBeCopied = obtainPsseBus(copyBus, busNumToPsseBus);
        PsseBus psseBus = psseBusToBeCopied.copy();
        psseBus.setI(newBus);
        psseBus.setName(obtainNewBusName(psseBus.getName().trim(), "-" + newBus));
        return psseBus;
    }

    // new bus name is obtained by adding "-" + newBus, but MAX_BUS_LENGTH must be ensured
    private static String obtainNewBusName(String baseName, String tag) {
        int newLength = baseName.length() + tag.length();
        if (newLength < MAX_BUS_LENGTH) {
            return StringUtils.rightPad(baseName + tag, MAX_BUS_LENGTH, " ");
        } else if (newLength == MAX_BUS_LENGTH) {
            return baseName + tag;
        } else {
            return baseName.substring(0, MAX_BUS_LENGTH - tag.length()) + tag;
        }
    }

    private static PsseBus obtainPsseBus(int bus, Map<Integer, PsseBus> busNumToPsseBus) {
        if (busNumToPsseBus.containsKey(bus)) {
            return busNumToPsseBus.get(bus);
        } else {
            throw new PsseException("Unexpected null PsseBus for " + bus);
        }
    }

    private static void updatePsseBus(Network network, String busId, PsseBus psseBus, int type) {
        Bus bus = network.getBusBreakerView().getBus(busId);
        if (bus == null) {
            psseBus.setVm(0.0);
            psseBus.setVa(0.0);
            psseBus.setIde(4);
        } else {
            psseBus.setVm(getVm(bus));
            psseBus.setVa(getVa(bus));
            psseBus.setIde(type);
        }
    }

    private static double getVm(Bus bus) {
        return Double.isFinite(bus.getV()) && bus.getV() > 0.0 ? bus.getV() / bus.getVoltageLevel().getNominalV() : 1.0;
    }

    private static double getVa(Bus bus) {
        return Double.isFinite(bus.getAngle()) ? bus.getAngle() : 0.0;
    }

    private final PsseBus psseBus;
    private final NodeBreakerImport nodeBreakerImport;
}
