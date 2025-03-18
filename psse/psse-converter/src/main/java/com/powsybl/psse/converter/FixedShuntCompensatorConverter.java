/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import java.util.Objects;
import java.util.OptionalInt;

import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.model.pf.PsseFixedShunt;
import com.powsybl.psse.model.pf.PssePowerFlowModel;

import static com.powsybl.psse.converter.AbstractConverter.PsseEquipmentType.PSSE_FIXED_SHUNT;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class FixedShuntCompensatorConverter extends AbstractConverter {

    FixedShuntCompensatorConverter(PsseFixedShunt psseFixedShunt, ContainersMapping containerMapping, Network network, NodeBreakerImport nodeBreakerImport) {
        super(containerMapping, network);
        this.psseFixedShunt = Objects.requireNonNull(psseFixedShunt);
        this.nodeBreakerImport = Objects.requireNonNull(nodeBreakerImport);
    }

    void create() {
        if (!getContainersMapping().isBusDefined(psseFixedShunt.getI())) {
            return;
        }
        if (psseFixedShunt.getGl() == 0 && psseFixedShunt.getBl() == 0.0) {
            LOGGER.warn("Shunt ({}) has Gl and Bl = 0, not imported ", psseFixedShunt.getI());
            return;
        }

        VoltageLevel voltageLevel = getNetwork()
            .getVoltageLevel(getContainersMapping().getVoltageLevelId(psseFixedShunt.getI()));
        ShuntCompensatorAdder adder = voltageLevel.newShuntCompensator()
            .setId(getFixedShuntId(psseFixedShunt.getI(), psseFixedShunt.getId()))
            .setVoltageRegulatorOn(false)
            .setSectionCount(1);
        adder.newLinearModel()
            .setGPerSection(powerToShuntAdmittance(psseFixedShunt.getGl(), voltageLevel.getNominalV()))
            .setBPerSection(powerToShuntAdmittance(psseFixedShunt.getBl(), voltageLevel.getNominalV()))
            .setMaximumSectionCount(1)
            .add();

        String equipmentId = getNodeBreakerEquipmentId(PSSE_FIXED_SHUNT, psseFixedShunt.getI(), psseFixedShunt.getId());
        OptionalInt node = nodeBreakerImport.getNode(getNodeBreakerEquipmentIdBus(equipmentId, psseFixedShunt.getI()));
        if (node.isPresent()) {
            adder.setNode(node.getAsInt());
        } else {
            String busId = getBusId(psseFixedShunt.getI());
            adder.setConnectableBus(busId);
            adder.setBus(psseFixedShunt.getStatus() == 1 ? busId : null);
        }

        adder.add();
    }

    static void update(Network network, PssePowerFlowModel psseModel) {
        psseModel.getFixedShunts().forEach(psseFixedShunt -> {
            String fixedShuntId = getFixedShuntId(psseFixedShunt.getI(), psseFixedShunt.getId());
            ShuntCompensator fixedShunt = network.getShuntCompensator(fixedShuntId);

            if (fixedShunt == null) {
                psseFixedShunt.setStatus(0);
            } else {
                psseFixedShunt.setStatus(getStatus(fixedShunt));
                psseFixedShunt.setGl(getP(fixedShunt));
                psseFixedShunt.setBl(getQ(fixedShunt));
            }
        });
    }

    private static double getP(ShuntCompensator shuntCompensator) {
        return shuntAdmittanceToPower(shuntCompensator.getG(shuntCompensator.getSectionCount()),
                shuntCompensator.getTerminal().getVoltageLevel().getNominalV());
    }

    private static double getQ(ShuntCompensator shuntCompensator) {
        return shuntAdmittanceToPower(shuntCompensator.getB(shuntCompensator.getSectionCount()),
                shuntCompensator.getTerminal().getVoltageLevel().getNominalV());
    }

    private final PsseFixedShunt psseFixedShunt;
    private final NodeBreakerImport nodeBreakerImport;

    private static final Logger LOGGER = LoggerFactory.getLogger(FixedShuntCompensatorConverter.class);
}
