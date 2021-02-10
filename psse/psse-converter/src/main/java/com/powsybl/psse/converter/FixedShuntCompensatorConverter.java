/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.converter;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensatorAdder;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.model.pf.PsseFixedShunt;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class FixedShuntCompensatorConverter extends AbstractConverter {

    public FixedShuntCompensatorConverter(PsseFixedShunt psseFixedShunt, ContainersMapping containerMapping, Network network) {
        super(containerMapping, network);
        this.psseFixedShunt = Objects.requireNonNull(psseFixedShunt);
    }

    public void create() {
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
        return busId + "-SH" + psseFixedShunt.getId();
    }

    private final PsseFixedShunt psseFixedShunt;

    private static final Logger LOGGER = LoggerFactory.getLogger(FixedShuntCompensatorConverter.class);
}
