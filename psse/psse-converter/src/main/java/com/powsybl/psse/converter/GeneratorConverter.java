/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.pf.PsseBus;
import com.powsybl.psse.model.pf.PsseGenerator;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class GeneratorConverter extends AbstractConverter {

    public GeneratorConverter(PsseGenerator psseGenerator, ContainersMapping containerMapping, Network network) {
        super(containerMapping, network);
        this.psseGenerator = psseGenerator;
    }

    public void create() {
        String busId = getBusId(psseGenerator.getI());
        VoltageLevel voltageLevel = getNetwork().getVoltageLevel(getContainersMapping().getVoltageLevelId(psseGenerator.getI()));
        Generator generator = voltageLevel.newGenerator()
                .setId(getGeneratorId(busId, psseGenerator))
                .setConnectableBus(busId)
                .setTargetP(psseGenerator.getPg())
                .setMaxP(psseGenerator.getPt())
                .setMinP(psseGenerator.getPb())
                .setTargetQ(psseGenerator.getQg())
                .setVoltageRegulatorOn(false)
                .add();

        generator.newMinMaxReactiveLimits()
                .setMinQ(psseGenerator.getQb())
                .setMaxQ(psseGenerator.getQt())
            .add();

        if (psseGenerator.getRt() != 0.0 || psseGenerator.getXt() != 0.0) {
            LOGGER.warn("Implicit method where a transformer is specified with the generator is not supported ({})", generator.getId());
        }

        if (psseGenerator.getStat() == 1) {
            generator.getTerminal().connect();
        }
    }

    public void addControl(PsseBus psseBus) {
        String busId = getBusId(psseGenerator.getI());
        Generator generator = getNetwork().getGenerator(getGeneratorId(busId, psseGenerator));

        // Add control only if generator has been created
        if (generator == null) {
            return;
        }

        boolean psseVoltageRegulatorOn = defineVoltageRegulatorOn(psseBus);
        Terminal regulatingTerminal = defineRegulatingTerminal(psseGenerator, getNetwork());
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
            throw new PsseException("PSSE. Generator " + defaultRegulatingBusId + "-"
                + getGeneratorId(defaultRegulatingBusId, psseGenerator) + ". Unexpected regulatingTerminal.");
        }
        return regulatingTerminal;
    }

    private static String getGeneratorId(String busId, PsseGenerator psseGenerator) {
        return busId + "-G" + psseGenerator.getId();
    }

    private final PsseGenerator psseGenerator;

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratorConverter.class);
}
