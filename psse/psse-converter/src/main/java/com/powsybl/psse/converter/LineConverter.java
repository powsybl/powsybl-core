/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import org.apache.commons.math3.complex.Complex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.CurrentLimitsAdder;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.LineAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.converter.PsseImporter.PerUnitContext;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.pf.PsseNonTransformerBranch;
import com.powsybl.psse.model.pf.PssePowerFlowModel;

import static com.powsybl.psse.converter.AbstractConverter.PsseEquipmentType.PSSE_BRANCH;
import static com.powsybl.psse.model.PsseVersion.Major.V35;

import java.util.Objects;
import java.util.OptionalInt;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class LineConverter extends AbstractConverter {

    LineConverter(PsseNonTransformerBranch psseLine, ContainersMapping containerMapping, PerUnitContext perUnitContext, Network network, PsseVersion version, NodeBreakerImport nodeBreakerImport) {
        super(containerMapping, network);
        this.psseLine = Objects.requireNonNull(psseLine);
        this.perUnitContext = Objects.requireNonNull(perUnitContext);
        this.version = Objects.requireNonNull(version);
        this.nodeBreakerImport = nodeBreakerImport;
    }

    void create() {
        String id = getLineId(psseLine.getI(), psseLine.getJ(), psseLine.getCkt());

        String voltageLevel1Id = getContainersMapping().getVoltageLevelId(psseLine.getI());
        String voltageLevel2Id = getContainersMapping().getVoltageLevelId(psseLine.getJ());

        VoltageLevel voltageLevel1 = getNetwork().getVoltageLevel(voltageLevel1Id);
        VoltageLevel voltageLevel2 = getNetwork().getVoltageLevel(voltageLevel2Id);

        // Support lines with different nominal voltage at ends
        double rEu = impedanceToEngineeringUnitsForLinesWithDifferentNominalVoltageAtEnds(psseLine.getR(), voltageLevel1.getNominalV(), voltageLevel2.getNominalV(), perUnitContext.sb());
        double xEu = impedanceToEngineeringUnitsForLinesWithDifferentNominalVoltageAtEnds(psseLine.getX(), voltageLevel1.getNominalV(), voltageLevel2.getNominalV(), perUnitContext.sb());
        Complex yEu = new Complex(rEu, xEu).reciprocal();
        double g1Eu = admittanceEnd1ToEngineeringUnitsForLinesWithDifferentNominalVoltageAtEnds(yEu.getReal(), psseLine.getGi(), voltageLevel1.getNominalV(), voltageLevel2.getNominalV(), perUnitContext.sb());
        double b1Eu = admittanceEnd1ToEngineeringUnitsForLinesWithDifferentNominalVoltageAtEnds(yEu.getImaginary(), psseLine.getB() * 0.5 + psseLine.getBi(), voltageLevel1.getNominalV(), voltageLevel2.getNominalV(), perUnitContext.sb());
        double g2Eu = admittanceEnd2ToEngineeringUnitsForLinesWithDifferentNominalVoltageAtEnds(yEu.getReal(), psseLine.getGj(), voltageLevel1.getNominalV(), voltageLevel2.getNominalV(), perUnitContext.sb());
        double b2Eu = admittanceEnd2ToEngineeringUnitsForLinesWithDifferentNominalVoltageAtEnds(yEu.getImaginary(), psseLine.getB() * 0.5 + psseLine.getBj(), voltageLevel1.getNominalV(), voltageLevel2.getNominalV(), perUnitContext.sb());

        LineAdder adder = getNetwork().newLine()
            .setId(id)
            .setEnsureIdUnicity(true)
            .setVoltageLevel1(voltageLevel1Id)
            .setVoltageLevel2(voltageLevel2Id)
            .setR(rEu)
            .setX(xEu)
            .setG1(g1Eu)
            .setB1(b1Eu)
            .setG2(g2Eu)
            .setB2(b2Eu);

        String equipmentId = getNodeBreakerEquipmentId(PSSE_BRANCH, psseLine.getI(), psseLine.getJ(), psseLine.getCkt());
        OptionalInt node1 = nodeBreakerImport.getNode(getNodeBreakerEquipmentIdBus(equipmentId, psseLine.getI()));
        if (node1.isPresent()) {
            adder.setNode1(node1.getAsInt());
        } else {
            String bus1Id = getBusId(psseLine.getI());
            adder.setConnectableBus1(bus1Id);
            adder.setBus1(psseLine.getSt() == 1 ? bus1Id : null);
        }
        OptionalInt node2 = nodeBreakerImport.getNode(getNodeBreakerEquipmentIdBus(equipmentId, psseLine.getJ()));
        if (node2.isPresent()) {
            adder.setNode2(node2.getAsInt());
        } else {
            String bus2Id = getBusId(psseLine.getJ());
            adder.setConnectableBus2(bus2Id);
            adder.setBus2(psseLine.getSt() == 1 ? bus2Id : null);
        }
        Line line = adder.add();

        defineOperationalLimits(line, voltageLevel1.getNominalV(), voltageLevel2.getNominalV());

        if (psseLine.getGi() != 0 || psseLine.getGj() != 0) {
            LOGGER.warn("Branch G not supported ({})", psseLine.getI());
        }
    }

    private void defineOperationalLimits(Line line, double vnom1, double vnom2) {
        double rateMva;
        if (version.major() == V35) {
            rateMva = psseLine.getRates().getRate1();
        } else {
            rateMva = psseLine.getRates().getRatea();
        }

        double currentLimit1 = rateMva / (Math.sqrt(3.0) * vnom1);
        double currentLimit2 = rateMva / (Math.sqrt(3.0) * vnom2);

        // CurrentPermanentLimit in A
        if (currentLimit1 > 0) {
            CurrentLimitsAdder currentLimitFrom = line.newCurrentLimits1();
            currentLimitFrom.setPermanentLimit(currentLimit1 * 1000);
            currentLimitFrom.add();
        }

        if (currentLimit2 > 0) {
            CurrentLimitsAdder currentLimitTo = line.newCurrentLimits2();
            currentLimitTo.setPermanentLimit(currentLimit2 * 1000);
            currentLimitTo.add();
        }
    }

    static void update(Network network, PssePowerFlowModel psseModel) {
        psseModel.getNonTransformerBranches().forEach(psseLine -> {
            String lineId = getLineId(psseLine.getI(), psseLine.getJ(), psseLine.getCkt());
            Line line = network.getLine(lineId);

            if (line == null) {
                psseLine.setSt(0);
            } else {
                psseLine.setSt(getStatus(line));
            }
        });
    }

    private static int getStatus(Line line) {
        if (line.getTerminal1().isConnected() && line.getTerminal1().getBusBreakerView().getBus() != null
                && line.getTerminal2().isConnected() && line.getTerminal2().getBusBreakerView().getBus() != null) {
            return 1;
        } else {
            return 0;
        }
    }

    private final PsseNonTransformerBranch psseLine;
    private final PerUnitContext perUnitContext;
    private final PsseVersion version;
    private final NodeBreakerImport nodeBreakerImport;

    private static final Logger LOGGER = LoggerFactory.getLogger(LineConverter.class);
}
