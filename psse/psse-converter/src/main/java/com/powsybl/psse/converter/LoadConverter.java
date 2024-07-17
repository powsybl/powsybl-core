/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import java.util.*;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.model.pf.PsseLoad;
import com.powsybl.psse.model.pf.PssePowerFlowModel;

import static com.powsybl.psse.converter.AbstractConverter.PsseEquipmentType.PSSE_LOAD;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class LoadConverter extends AbstractConverter {

    LoadConverter(PsseLoad psseLoad, ContainersMapping containerMapping, Network network, NodeBreakerImport nodeBreakerImport) {
        super(containerMapping, network);
        this.psseLoad = Objects.requireNonNull(psseLoad);
        this.nodeBreakerImport = Objects.requireNonNull(nodeBreakerImport);
    }

    void create() {

        VoltageLevel voltageLevel = getNetwork()
            .getVoltageLevel(getContainersMapping().getVoltageLevelId(psseLoad.getI()));

        double p0 = psseLoad.getPl() + psseLoad.getIp() + psseLoad.getYp();
        double q0 = psseLoad.getQl() + psseLoad.getIq() + psseLoad.getYq();

        LoadAdder adder = voltageLevel.newLoad()
            .setId(getLoadId(psseLoad.getI(), psseLoad.getId()))

            .setP0(p0)
            .setQ0(q0);

        boolean constantPower = psseLoad.getIp() == 0 && psseLoad.getYp() == 0 && psseLoad.getIq() == 0 && psseLoad.getYq() == 0;
        if (!constantPower && (p0 != 0 || q0 != 0)) {
            double c0p;
            double c1p;
            double c2p;
            if (p0 != 0) {
                c0p = psseLoad.getPl() / p0;
                c1p = psseLoad.getIp() / p0;
                c2p = psseLoad.getYp() / p0;
            } else {
                c0p = 1;
                c1p = 0;
                c2p = 0;
            }
            double c0q;
            double c1q;
            double c2q;
            if (q0 != 0) {
                c0q = psseLoad.getQl() / q0;
                c1q = psseLoad.getIq() / q0;
                c2q = psseLoad.getYq() / q0;
            } else {
                c0q = 1;
                c1q = 0;
                c2q = 0;
            }
            adder.newZipModel()
                    .setC0p(c0p)
                    .setC1p(c1p)
                    .setC2p(c2p)
                    .setC0q(c0q)
                    .setC1q(c1q)
                    .setC2q(c2q)
                    .add();
        }

        String equipmentId = getNodeBreakerEquipmentId(PSSE_LOAD, psseLoad.getI(), psseLoad.getId());
        OptionalInt node = nodeBreakerImport.getNode(getNodeBreakerEquipmentIdBus(equipmentId, psseLoad.getI()));
        if (node.isPresent()) {
            adder.setNode(node.getAsInt());
        } else {
            String busId = getBusId(psseLoad.getI());
            adder.setConnectableBus(busId);
            adder.setBus(psseLoad.getStatus() == 1 ? busId : null);
        }

        adder.add();
    }

    static void updateAndCreateLoads(Network network, PssePowerFlowModel psseModel, ContextExport contextExport) {
        Map<String, PsseLoad> loadsToPsseLoad = new HashMap<>();
        psseModel.getLoads().forEach(psseLoad -> loadsToPsseLoad.put(getLoadId(psseLoad.getI(), psseLoad.getId()), psseLoad));

        network.getLoads().forEach(load -> {
            if (loadsToPsseLoad.containsKey(load.getId())) {
                updateLoad(load, loadsToPsseLoad.get(load.getId()), contextExport);
            } else {
                psseModel.addLoads(Collections.singletonList(createLoad(load, contextExport)));
            }
        });
        psseModel.replaceAllLoads(psseModel.getLoads().stream().sorted(Comparator.comparingInt(PsseLoad::getI).thenComparing(PsseLoad::getId)).toList());
    }

    private static void updateLoad(Load load, PsseLoad psseLoad, ContextExport contextExport) {
        int bus = getTerminalBusI(load.getTerminal(), contextExport);
        psseLoad.setStatus(getStatus(load.getTerminal()));
        psseLoad.setPl(getP(load));
        psseLoad.setQl(getQ(load));
        psseLoad.setI(bus);
    }

    private static double getP(Load load) {
        if (Double.isNaN(load.getTerminal().getP())) {
            return load.getP0();
        } else {
            return load.getTerminal().getP();
        }
    }

    private static double getQ(Load load) {
        if (Double.isNaN(load.getTerminal().getQ())) {
            return load.getQ0();
        } else {
            return load.getTerminal().getQ();
        }
    }

    private static PsseLoad createLoad(Load load, ContextExport contextExport) {
        PsseLoad psseLoad = new PsseLoad();

        int busI = getTerminalBusI(load.getTerminal(), contextExport);
        psseLoad.setI(busI);
        psseLoad.setId(contextExport.getEquipmentCkt(load.getId(), IdentifiableType.LOAD, busI));
        psseLoad.setStatus(getStatus(load.getTerminal()));
        psseLoad.setArea(1);
        psseLoad.setZone(1);
        psseLoad.setPl(load.getP0());
        psseLoad.setQl(load.getQ0());
        psseLoad.setIp(0.0);
        psseLoad.setIq(0.0);
        psseLoad.setYp(0.0);
        psseLoad.setYq(0.0);
        psseLoad.setOwner(1);
        psseLoad.setScale(1);
        psseLoad.setIntrpt(0);
        psseLoad.setDgenp(0.0);
        psseLoad.setDgenq(0.0);
        psseLoad.setDgenm(0);
        String type = "";
        psseLoad.setLoadtype(type.substring(0, Math.min(12, type.length())));

        return psseLoad;
    }

    private final PsseLoad psseLoad;
    private final NodeBreakerImport nodeBreakerImport;
}
