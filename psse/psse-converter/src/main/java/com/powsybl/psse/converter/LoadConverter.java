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
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
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

        // Only constant power is considered at the moment
        // S = VI*, S = YVV*
        // .setIp(psseLoad.getIp())
        // .setIq(psseLoad.getIq())
        // .setYp(psseLoad.getYp())
        // .setYq(psseLoad.getYq())

        LoadAdder adder = voltageLevel.newLoad()
            .setId(getLoadId(busId))
            .setConnectableBus(busId)
            .setP0(psseLoad.getPl())
            .setQ0(psseLoad.getQl());

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
