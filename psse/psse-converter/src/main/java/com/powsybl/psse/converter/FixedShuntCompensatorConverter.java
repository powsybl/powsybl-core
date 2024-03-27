/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import java.util.Objects;

import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.model.pf.PsseFixedShunt;
import com.powsybl.psse.model.pf.PssePowerFlowModel;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class FixedShuntCompensatorConverter extends AbstractConverter {

    FixedShuntCompensatorConverter(PsseFixedShunt psseFixedShunt, ContainersMapping containerMapping, Network network) {
        super(containerMapping, network);
        this.psseFixedShunt = Objects.requireNonNull(psseFixedShunt);
    }

    void create() {
        if (psseFixedShunt.getGl() == 0 && psseFixedShunt.getBl() == 0.0) {
            LOGGER.warn("Shunt ({}) has Gl and Bl = 0, not imported ", psseFixedShunt.getI());
            return;
        }

        String busId = getBusId(psseFixedShunt.getI());
        VoltageLevel voltageLevel = getNetwork()
            .getVoltageLevel(getContainersMapping().getVoltageLevelId(psseFixedShunt.getI()));
        ShuntCompensatorAdder adder = voltageLevel.newShuntCompensator()
            .setId(getShuntId(busId))
            .setConnectableBus(busId)
            .setVoltageRegulatorOn(false)
            .setSectionCount(1);
        adder.newLinearModel()
            .setGPerSection(powerToShuntAdmittance(psseFixedShunt.getGl(), voltageLevel.getNominalV()))
            .setBPerSection(powerToShuntAdmittance(psseFixedShunt.getBl(), voltageLevel.getNominalV()))
            .setMaximumSectionCount(1)
            .add();

        adder.setBus(psseFixedShunt.getStatus() == 1 ? busId : null);
        adder.add();
    }

    private String getShuntId(String busId) {
        return getShuntId(busId, psseFixedShunt.getId());
    }

    private static String getShuntId(String busId, String fixedShuntId) {
        return busId + "-SH" + fixedShuntId;
    }

    // At the moment we do not consider new fixedShunts
    static void updateFixedShunts(Network network, PssePowerFlowModel psseModel) {
        psseModel.getFixedShunts().forEach(psseFixedShunt -> {
            String fixedShuntId = getShuntId(getBusId(psseFixedShunt.getI()), psseFixedShunt.getId());
            ShuntCompensator fixedShunt = network.getShuntCompensator(fixedShuntId);
            if (fixedShunt == null) {
                psseFixedShunt.setStatus(0);
            } else {
                psseFixedShunt.setStatus(getStatus(fixedShunt));
                psseFixedShunt.setBl(getQ(fixedShunt));
            }
        });
    }

    private static int getStatus(ShuntCompensator fixedShunt) {
        if (fixedShunt.getTerminal().isConnected()) {
            return 1;
        } else {
            return 0;
        }
    }

    private static double getQ(ShuntCompensator fixedShunt) {
        return shuntAdmittanceToPower(fixedShunt.getB(fixedShunt.getSectionCount()),
            fixedShunt.getTerminal().getVoltageLevel().getNominalV());
    }

    private final PsseFixedShunt psseFixedShunt;

    private static final Logger LOGGER = LoggerFactory.getLogger(FixedShuntCompensatorConverter.class);
}
