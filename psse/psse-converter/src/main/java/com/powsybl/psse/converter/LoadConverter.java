/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.converter;

import java.util.Collections;
import java.util.Objects;

import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.LoadAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.model.pf.PsseLoad;
import com.powsybl.psse.model.pf.PssePowerFlowModel;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class LoadConverter extends AbstractConverter {

    LoadConverter(PsseLoad psseLoad, ContainersMapping containerMapping, Network network) {
        super(containerMapping, network);
        this.psseLoad = Objects.requireNonNull(psseLoad);
    }

    void create() {

        String busId = getBusId(psseLoad.getI());
        VoltageLevel voltageLevel = getNetwork()
            .getVoltageLevel(getContainersMapping().getVoltageLevelId(psseLoad.getI()));

        double p0 = psseLoad.getPl() + psseLoad.getIp() + psseLoad.getYp();
        double q0 = psseLoad.getQl() + psseLoad.getIq() + psseLoad.getYq();

        LoadAdder adder = voltageLevel.newLoad()
            .setId(getLoadId(busId))
            .setConnectableBus(busId)
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

        adder.setBus(psseLoad.getStatus() == 1 ? busId : null);
        adder.add();
    }

    private String getLoadId(String busId) {
        return getLoadId(busId, psseLoad.getId());
    }

    private static String getLoadId(String busId, String loadId) {
        return busId + "-L" + loadId;
    }

    // At the moment we do not consider new loads
    static void updateLoads(Network network, PssePowerFlowModel psseModel, PssePowerFlowModel updatePsseModel) {
        psseModel.getLoads().forEach(psseLoad -> {
            updatePsseModel.addLoads(Collections.singletonList(psseLoad));
            PsseLoad updatePsseLoad = updatePsseModel.getLoads().get(updatePsseModel.getLoads().size() - 1);

            String loadId = getLoadId(getBusId(updatePsseLoad.getI()), updatePsseLoad.getId());
            Load load = network.getLoad(loadId);
            if (load == null) {
                updatePsseLoad.setStatus(0);
            } else {
                updatePsseLoad.setStatus(getStatus(load));
                updatePsseLoad.setPl(getP(load));
                updatePsseLoad.setQl(getQ(load));
            }
        });
    }

    private static int getStatus(Load load) {
        if (load.getTerminal().isConnected()) {
            return 1;
        } else {
            return 0;
        }
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

    private final PsseLoad psseLoad;
}
