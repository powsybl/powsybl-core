/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.converter;

import java.util.Map;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.converter.PsseImporter.ShuntBlockTab;
import com.powsybl.psse.model.pf.PsseSwitchedShunt;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class SwitchedShuntCompensatorConverter extends AbstractConverter {

    public SwitchedShuntCompensatorConverter(PsseSwitchedShunt psseSwitchedShunt, ContainersMapping containerMapping, Network network) {
        super(containerMapping, network);
        this.psseSwitchedShunt = psseSwitchedShunt;
    }

    public void create(Map<PsseSwitchedShunt, ShuntBlockTab> stoBlockiTab) {
        String busId = getBusId(psseSwitchedShunt.getI());
        VoltageLevel voltageLevel = getNetwork().getVoltageLevel(getContainersMapping().getVoltageLevelId(psseSwitchedShunt.getI()));
        ShuntBlockTab sbl = stoBlockiTab.get(psseSwitchedShunt);

        for (int i = 1; i <= sbl.getSize(); i++) {
            if (psseSwitchedShunt.getBinit() != 0) { //TODO : improve it to make it robust to all configurations
                ShuntCompensator shunt = voltageLevel.newShuntCompensator()
                        .setId(getShuntId(busId, i))
                        .setConnectableBus(busId)
                        .setSectionCount(1)
                        .newLinearModel() //TODO: use Binit and sbl.getNi(i) to initiate Bi, for now we use Binit to obtain de same load-flow results
                            .setBPerSection(psseSwitchedShunt.getBinit())//TODO: take into account BINIT to define the number of switched steps in the case BINIT is different from the max switched steps
                            .setMaximumSectionCount(1)
                        .add()
                    .add();

                if (psseSwitchedShunt.getStat() == 1) {
                    shunt.getTerminal().connect();
                }
            }
        }
    }

    private String getShuntId(String busId, int i) {
        return busId + "-SwSH-B" + i;
    }

    private final PsseSwitchedShunt psseSwitchedShunt;
}
