/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.GeneratorAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.converter.NodeBreakerImport.NodeBreakerControlNode;
import com.powsybl.psse.model.pf.PsseBus;
import com.powsybl.psse.model.pf.PsseGenerator;
import com.powsybl.psse.model.pf.PssePowerFlowModel;

import static com.powsybl.psse.converter.AbstractConverter.PsseEquipmentType.PSSE_GENERATOR;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class GeneratorConverter extends AbstractConverter {

    GeneratorConverter(PsseGenerator psseGenerator, ContainersMapping containerMapping, Network network, NodeBreakerImport nodeBreakerImport) {
        super(containerMapping, network);
        this.psseGenerator = Objects.requireNonNull(psseGenerator);
        this.nodeBreakerImport = Objects.requireNonNull(nodeBreakerImport);
    }

    void create() {
        VoltageLevel voltageLevel = getNetwork().getVoltageLevel(getContainersMapping().getVoltageLevelId(psseGenerator.getI()));
        GeneratorAdder adder = voltageLevel.newGenerator()
                .setId(getGeneratorId(psseGenerator.getI(), psseGenerator.getId()))
                .setTargetP(psseGenerator.getPg())
                .setMaxP(psseGenerator.getPt())
                .setMinP(psseGenerator.getPb())
                .setTargetQ(psseGenerator.getQg())
                .setVoltageRegulatorOn(false);

        String equipmentId = getNodeBreakerEquipmentId(PSSE_GENERATOR, psseGenerator.getI(), psseGenerator.getId());
        OptionalInt node = nodeBreakerImport.getNode(getNodeBreakerEquipmentIdBus(equipmentId, psseGenerator.getI()));
        if (node.isPresent()) {
            adder.setNode(node.getAsInt());
        } else {
            String busId = getBusId(psseGenerator.getI());
            adder.setConnectableBus(busId);
            adder.setBus(psseGenerator.getStat() == 1 ? busId : null);
        }

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
        Generator generator = getNetwork().getGenerator(getGeneratorId(psseGenerator.getI(), psseGenerator.getId()));

        // Add control only if generator has been created
        if (generator == null) {
            return;
        }

        Terminal regulatingTerminal = defineRegulatingTerminal(psseGenerator, getNetwork(), generator, nodeBreakerImport);
        // Discard control if the generator is controlling an isolated bus
        if (regulatingTerminal == null) {
            return;
        }
        boolean psseVoltageRegulatorOn = defineVoltageRegulatorOn(psseBus);
        double vnom = regulatingTerminal.getVoltageLevel().getNominalV();
        double targetV = psseGenerator.getVs() * vnom;

        boolean voltageRegulatorOn = false;
        // we consider < but psse accepts bus type 2 with Qmin == Qmax
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

    private static Terminal defineRegulatingTerminal(PsseGenerator psseGenerator, Network network, Generator generator, NodeBreakerImport nodeBreakerImport) {
        Terminal regulatingTerminal = null;
        if (psseGenerator.getIreg() == 0) {
            regulatingTerminal = generator.getTerminal();
        } else {
            String equipmentId = getNodeBreakerEquipmentId(PSSE_GENERATOR, psseGenerator.getI(), psseGenerator.getId());
            Optional<NodeBreakerControlNode> controlNode = nodeBreakerImport.getControlNode(getNodeBreakerEquipmentIdBus(equipmentId, psseGenerator.getIreg()));
            if (controlNode.isPresent()) {
                regulatingTerminal = findTerminalNode(network, controlNode.get().getVoltageLevelId(), controlNode.get().getNode());
            } else {
                String regulatingBusId = getBusId(psseGenerator.getIreg());
                Bus bus = network.getBusBreakerView().getBus(regulatingBusId);
                if (bus != null) {
                    regulatingTerminal = bus.getConnectedTerminalStream().findFirst().orElse(null);
                }
            }
        }
        if (regulatingTerminal == null && psseGenerator.getI() != psseGenerator.getIreg()) {
            String generatorId = getGeneratorId(psseGenerator.getI(), psseGenerator.getId());
            LOGGER.warn("Generator {}. Regulating terminal is not assigned as the bus is isolated", generatorId);
        }
        return regulatingTerminal;
    }

    static void updateGenerators(Network network, PssePowerFlowModel psseModel) {
        psseModel.getGenerators().forEach(psseGen -> {
            String genId = getGeneratorId(psseGen.getI(), psseGen.getId());
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
        if (gen.getTerminal().isConnected() && gen.getTerminal().getBusBreakerView().getBus() != null) {
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
    private final NodeBreakerImport nodeBreakerImport;

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratorConverter.class);
}
