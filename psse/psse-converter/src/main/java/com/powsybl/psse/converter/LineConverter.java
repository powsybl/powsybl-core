/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.converter;

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

import static com.powsybl.psse.model.PsseVersion.Major.V35;

import java.util.Collections;
import java.util.Objects;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class LineConverter extends AbstractConverter {

    public LineConverter(PsseNonTransformerBranch psseLine, ContainersMapping containerMapping, PerUnitContext perUnitContext, Network network, PsseVersion version) {
        super(containerMapping, network);
        this.psseLine = Objects.requireNonNull(psseLine);
        this.perUnitContext = Objects.requireNonNull(perUnitContext);
        this.version = Objects.requireNonNull(version);
    }

    public void create() {
        String id = getLineId();

        String bus1Id = getBusId(psseLine.getI());
        String bus2Id = getBusId(psseLine.getJ());
        String voltageLevel1Id = getContainersMapping().getVoltageLevelId(psseLine.getI());
        String voltageLevel2Id = getContainersMapping().getVoltageLevelId(psseLine.getJ());
        VoltageLevel voltageLevel2 = getNetwork().getVoltageLevel(voltageLevel2Id);

        LineAdder adder = getNetwork().newLine()
            .setId(id)
            .setEnsureIdUnicity(true)
            .setConnectableBus1(bus1Id)
            .setVoltageLevel1(voltageLevel1Id)
            .setConnectableBus2(bus2Id)
            .setVoltageLevel2(voltageLevel2Id)
            .setR(impedanceToEngineeringUnits(psseLine.getR(), voltageLevel2.getNominalV(), perUnitContext.getSb()))
            .setX(impedanceToEngineeringUnits(psseLine.getX(), voltageLevel2.getNominalV(), perUnitContext.getSb()))
            .setG1(admittanceToEngineeringUnits(psseLine.getGi(), voltageLevel2.getNominalV(), perUnitContext.getSb()))
            .setB1(admittanceToEngineeringUnits(psseLine.getB() * 0.5 + psseLine.getBi(), voltageLevel2.getNominalV(), perUnitContext.getSb()))
            .setG2(admittanceToEngineeringUnits(psseLine.getGj(), voltageLevel2.getNominalV(), perUnitContext.getSb()))
            .setB2(admittanceToEngineeringUnits(psseLine.getB() * 0.5 + psseLine.getBj(), voltageLevel2.getNominalV(), perUnitContext.getSb()));

        adder.setBus1(psseLine.getSt() == 1 ? bus1Id : null);
        adder.setBus2(psseLine.getSt() == 1 ? bus2Id : null);
        Line line = adder.add();

        VoltageLevel voltageLevel1 = getNetwork().getVoltageLevel(voltageLevel1Id);
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

    private String getLineId() {
        return getLineId(psseLine);
    }

    private static String getLineId(PsseNonTransformerBranch psseLine) {
        return "L-" + psseLine.getI() + "-" + psseLine.getJ() + "-" + psseLine.getCkt();
    }

    // At the moment we do not consider new lines and antenna lines are exported as open
    static void updateLines(Network network, PssePowerFlowModel psseModel, PssePowerFlowModel updatePsseModel) {
        psseModel.getNonTransformerBranches().forEach(psseLine -> {
            updatePsseModel.addNonTransformerBranches(Collections.singletonList(psseLine));
            PsseNonTransformerBranch updatePsseLine = updatePsseModel.getNonTransformerBranches().get(updatePsseModel.getNonTransformerBranches().size() - 1);

            String lineId = getLineId(updatePsseLine);
            Line line = network.getLine(lineId);
            if (line == null) {
                updatePsseLine.setSt(0);
            } else {
                if (line.getTerminal1().isConnected() && line.getTerminal2().isConnected()) {
                    updatePsseLine.setSt(1);
                } else {
                    updatePsseLine.setSt(0);
                }
            }
        });
    }

    private final PsseNonTransformerBranch psseLine;
    private final PerUnitContext perUnitContext;
    private final PsseVersion version;

    private static final Logger LOGGER = LoggerFactory.getLogger(LineConverter.class);
}
