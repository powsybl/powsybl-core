/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.converter.PsseImporter.PerUnitContext;
import com.powsybl.psse.model.pf.PsseNonTransformerBranch;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class LineConverter extends AbstractConverter {

    public LineConverter(PsseNonTransformerBranch psseLine, ContainersMapping containerMapping, PerUnitContext perUnitContext, Network network) {
        super(containerMapping, network);
        this.psseLine = psseLine;
        this.perUnitContext = perUnitContext;
    }

    public void create() {
        String id = getLineId();

        String bus1Id = getBusId(psseLine.getI());
        String bus2Id = getBusId(psseLine.getJ());
        String voltageLevel1Id = getContainersMapping().getVoltageLevelId(psseLine.getI());
        String voltageLevel2Id = getContainersMapping().getVoltageLevelId(psseLine.getJ());
        VoltageLevel voltageLevel2 = getNetwork().getVoltageLevel(voltageLevel2Id);
        double zb = voltageLevel2.getNominalV() * voltageLevel2.getNominalV() / perUnitContext.getSb();

        Line line = getNetwork().newLine()
            .setId(id)
            .setEnsureIdUnicity(true)
            .setConnectableBus1(bus1Id)
            .setVoltageLevel1(voltageLevel1Id)
            .setConnectableBus2(bus2Id)
            .setVoltageLevel2(voltageLevel2Id)
            .setR(psseLine.getR() * zb)
            .setX(psseLine.getX() * zb)
            .setG1(psseLine.getGi() / zb)
            .setB1(psseLine.getB() / zb / 2 + psseLine.getBi() / zb)
            .setG2(psseLine.getGj() / zb)
            .setB2(psseLine.getB() / zb / 2 + psseLine.getBj() / zb)
            .add();

        if (psseLine.getSt() == 1) {
            line.getTerminal1().connect();
            line.getTerminal2().connect();
        }

        if (psseLine.getGi() != 0 || psseLine.getGj() != 0) {
            LOGGER.warn("Branch G not supported ({})", psseLine.getI());
        }
    }

    private String getLineId() {
        return "L-" + psseLine.getI() + "-" + psseLine.getJ() + "-" + psseLine.getCkt();
    }

    private final PsseNonTransformerBranch psseLine;
    private final PerUnitContext perUnitContext;

    private static final Logger LOGGER = LoggerFactory.getLogger(LineConverter.class);
}
