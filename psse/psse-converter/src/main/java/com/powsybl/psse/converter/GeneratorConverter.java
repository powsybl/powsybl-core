/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.GeneratorAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.model.pf.PsseBus;
import com.powsybl.psse.model.pf.PsseGenerator;
import com.powsybl.psse.model.pf.PssePowerFlowModel;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class GeneratorConverter extends AbstractConverter {

    GeneratorConverter(PsseGenerator psseGenerator, ContainersMapping containerMapping, Network network) {
        super(containerMapping, network);
        this.psseGenerator = Objects.requireNonNull(psseGenerator);
    }

    void create() {
        String busId = getBusId(psseGenerator.getI());
        VoltageLevel voltageLevel = getNetwork().getVoltageLevel(getContainersMapping().getVoltageLevelId(psseGenerator.getI()));
        GeneratorAdder adder = voltageLevel.newGenerator()
                .setId(getGeneratorId(busId, psseGenerator))
                .setConnectableBus(busId)
                .setTargetP(psseGenerator.getPg())
                .setMaxP(psseGenerator.getPt())
                .setMinP(psseGenerator.getPb())
                .setTargetQ(psseGenerator.getQg())
                .setVoltageRegulatorOn(false);

        adder.setBus(psseGenerator.getStat() == 1 ? busId : null);
        Generator generator = adder.add();

        generator.newMinMaxReactiveLimits()
                .setMinQ(psseGenerator.getQb())
                .setMaxQ(psseGenerator.getQt())
            .add();

        if (psseGenerator.getRt() != 0.0 || psseGenerator.getXt() != 0.0) {
            LOGGER.warn("Implicit method where a transformer is specified with the generator is not supported ({})", generator.getId());
        }
    }

    void addControl(PsseBus psseBus) {
        String busId = getBusId(psseGenerator.getI());
        Generator generator = getNetwork().getGenerator(getGeneratorId(busId, psseGenerator));

        // Add control only if generator has been created
        if (generator == null) {
            return;
        }

        Terminal regulatingTerminal = defineRegulatingTerminal(psseGenerator, getNetwork());
        // Discard control if the generator is controlling an isolated bus
        if (regulatingTerminal == null) {
            return;
        }
        boolean psseVoltageRegulatorOn = defineVoltageRegulatorOn(psseBus);
        double vnom = regulatingTerminal.getVoltageLevel().getNominalV();
        double targetV = psseGenerator.getVs() * vnom;
        boolean voltageRegulatorOn = false;
        if (targetV > 0.0 && psseGenerator.getQb() < psseGenerator.getQt()) {
            voltageRegulatorOn = psseVoltageRegulatorOn;
        }

        generator.setTargetV(targetV)
            .setRegulatingTerminal(regulatingTerminal)
            .setVoltageRegulatorOn(voltageRegulatorOn);
    }

    private static boolean defineVoltageRegulatorOn(PsseBus psseBus) {
        return psseBus.getIde() == 2 || psseBus.getIde() == 3;
    }

    // Nreg (version 35) is not yet considered
    private static Terminal defineRegulatingTerminal(PsseGenerator psseGenerator, Network network) {
        String defaultRegulatingBusId = getBusId(psseGenerator.getI());
        Terminal regulatingTerminal = null;
        if (psseGenerator.getIreg() == 0) {
            Bus bus = network.getBusBreakerView().getBus(defaultRegulatingBusId);
            regulatingTerminal = bus.getConnectedTerminalStream().findFirst().orElse(null);
        } else {
            String regulatingBusId = getBusId(psseGenerator.getIreg());
            Bus bus = network.getBusBreakerView().getBus(regulatingBusId);
            if (bus != null) {
                regulatingTerminal = bus.getConnectedTerminalStream().findFirst().orElse(null);
            }
        }
        if (regulatingTerminal == null) {
            String generatorId = getGeneratorId(defaultRegulatingBusId, psseGenerator);
            LOGGER.warn("Generator {}. Regulating terminal is not assigned as the bus is isolated", generatorId);
        }
        return regulatingTerminal;
    }

    private static String getGeneratorId(String busId, PsseGenerator psseGenerator) {
        return getGeneratorId(busId, psseGenerator.getId());
    }

    private static String getGeneratorId(String busId, String generatorId) {
        return busId + "-G" + generatorId;
    }

    // At the moment we do not consider new generators
    static void updateGenerators(Network network, PssePowerFlowModel psseModel) {
        psseModel.getGenerators().forEach(psseGen -> {
            String genId = getGeneratorId(getBusId(psseGen.getI()), psseGen.getId());
            Generator gen = network.getGenerator(genId);
            if (gen == null) {
                psseGen.setStat(0);
            } else {
                psseGen.setStat(getStatus(gen));
                psseGen.setPg(getP(gen));
                psseGen.setQg(getQ(gen));
            }
        });
    }

    private static int getStatus(Generator gen) {
        if (gen.getTerminal().isConnected()) {
            return 1;
        } else {
            return 0;
        }
    }

    private static double getP(Generator gen) {
        if (Double.isNaN(gen.getTerminal().getP())) {
            return gen.getTargetP();
        } else {
            return -gen.getTerminal().getP();
        }
    }

    private static double getQ(Generator gen) {
        if (Double.isNaN(gen.getTerminal().getQ())) {
            return gen.getTargetQ();
        } else {
            return -gen.getTerminal().getQ();
        }
    }

    private final PsseGenerator psseGenerator;

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratorConverter.class);
}
