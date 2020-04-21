/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.update.elements16.GeneratorToSynchronousMachine;
import com.powsybl.cgmes.conversion.update.elements16.TwoWindingsTransformerToPowerTransformer;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.MinMaxReactiveLimits;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ReactiveLimitsKind;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public final class NetworkChanges {
    private NetworkChanges() {
    }

    public static void scaleLoadGenerator(Network network, int maxChanges) {
        int count = 0;
        for (Generator g : network.getGenerators()) {
            double newP = g.getTargetP() * 1.1;
            double newQ = g.getTargetQ() * 1.1;
            g.setTargetP(newP).setTargetQ(newQ).getTerminal().setP(-newP).setQ(-newQ);
            count++;
            if (count > maxChanges) {
                break;
            }
        }

        count = 0;
        for (Load g : network.getLoads()) {
            double newP = g.getP0() * 1.1;
            double newQ = g.getQ0() * 1.1;
            g.setP0(newP).setQ0(newQ).getTerminal().setP(newP).setQ(newQ);
            count++;
            if (count > maxChanges) {
                break;
            }
        }
    }

    public static void modifyEquipmentCharacteristics(Network network) {
        VoltageLevel vl = network.getVoltageLevels().iterator().next();
        vl.setLowVoltageLimit(vl.getNominalV() - 3.1415);
        vl.setHighVoltageLimit(vl.getNominalV() + 3.1415);

        Line line = network.getLines().iterator().next();
        // Some lines may correspond to CGMES switches
        // Do not make changes on low impedance lines
        if (line.getR() < 1e-3 && line.getX() < 1e-3) {
            LOG.warn("No changes are made in IIDM low impedance lines");
        } else {
            line.setR(line.getR() + 0.8);
            line.setX(line.getX() + 0.8);
            // To be recovered in IIDM after updated CGMES we have to keep G1==G2 and B1==B2
            // as CGMES only has global gch, bch values for the line, not split between ends
            line.setG1(line.getG1() + 0.4);
            line.setG2(line.getG2() + 0.4);
            line.setB1(line.getB1() + 0.2);
            line.setB2(line.getB2() + 0.2);
        }

        TwoWindingsTransformerToPowerTransformer ct2 = new TwoWindingsTransformerToPowerTransformer();
        if (ct2.isSupported("ratedU1") && ct2.isSupported("ratedU22")) {
            modifyTwoWindingsTransformerRatedU(network);
        }
        if (ct2.isSupported("r") && ct2.isSupported("x")) {
            modifyTwoWindingsTransformerRX(network);
        }
        if (ct2.isSupported("g") && ct2.isSupported("b")) {
            modifyTwoWindingsTransformerGB(network);
        }

        GeneratorToSynchronousMachine cg = new GeneratorToSynchronousMachine();
        if (cg.isSupported("reactiveLimits")) {
            modifyGeneratorReactiveLimits(network);
        }

        if (network.getShuntCompensatorCount() > 0) {
            ShuntCompensator sh = network.getShuntCompensators().iterator().next();
            sh.setbPerSection(sh.getbPerSection() + 0.2);
            sh.setMaximumSectionCount(sh.getMaximumSectionCount() + 5);
        }
    }

    public static void modifySteadyStateHypothesis(Network network) {
        GeneratorToSynchronousMachine cg = new GeneratorToSynchronousMachine();
        if (cg.isSupported("targetV") && cg.isSupported("voltageRegulatorOn")) {
            modifyGeneratorVoltageRegulation(network);
        }
        modifyShuntCompensatorSections(network);
    }

    public static void modifyShuntCompensatorSections(Network network) {
        boolean found = false;
        for (ShuntCompensator sh : network.getShuntCompensators()) {
            int newSections = sh.getCurrentSectionCount() == 0 ? sh.getMaximumSectionCount() : 0;
            if (newSections != sh.getCurrentSectionCount()) {
                sh.setCurrentSectionCount(newSections);
                found = true;
            }
        }
        if (!found) {
            LOG.error("Did not find a ShuntCompensator to test");
        }
    }

    public static void modifyTwoWindingsTransformerRatedU(Network network) {
        TwoWindingsTransformer t2 = network.getTwoWindingsTransformers().iterator().next();
        t2.setRatedU1(t2.getRatedU1() + 10);
        t2.setRatedU2(t2.getRatedU2() - 10);
    }

    public static void modifyTwoWindingsTransformerRX(Network network) {
        TwoWindingsTransformer t2 = network.getTwoWindingsTransformers().iterator().next();
        t2.setR(t2.getR() + 0.8);
        t2.setX(t2.getX() + 0.8);
    }

    public static void modifyTwoWindingsTransformerGB(Network network) {
        TwoWindingsTransformer t2 = network.getTwoWindingsTransformers().iterator().next();
        t2.setG(t2.getG() + 0.1);
        t2.setB(t2.getB() + 0.1);
    }

    public static void modifyGeneratorReactiveLimits(Network network) {
        // Apply changes to first generator that has min/max limits for Q
        for (Generator g : network.getGenerators()) {
            if (g.getReactiveLimits().getKind() == ReactiveLimitsKind.MIN_MAX) {
                g.setRatedS(g.getRatedS() * 1.2);
                g.setMinP(g.getMinP() - 10);
                g.setMaxP(g.getMaxP() + 10);

                MinMaxReactiveLimits l = (MinMaxReactiveLimits) g.getReactiveLimits();
                g.newMinMaxReactiveLimits()
                    .setMinQ(l.getMinQ() - 10)
                    .setMaxQ(l.getMinQ() + 10)
                    .add();
                break;
            }
        }
    }

    public static void modifyGeneratorVoltageRegulation(Network network) {
        // Apply changes to first generator with voltage regulation active
        for (Generator g : network.getGenerators()) {
            if (g.isVoltageRegulatorOn()) {
                g.setTargetV(g.getTargetV() + 0.1);
                g.setVoltageRegulatorOn(false);
            }
        }
    }

    public static void modifyStateVariables(Network network) {
        network.getBusBreakerView().getBuses().forEach(b -> {
            b.setAngle(b.getAngle() + 0.01);
            b.setV(b.getV() + 0.01);
        });
    }

    private static final Logger LOG = LoggerFactory.getLogger(NetworkChanges.class);
}
