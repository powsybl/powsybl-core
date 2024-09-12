/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import java.util.*;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.pf.PsseFacts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.powsybl.psse.converter.AbstractConverter.PsseEquipmentType.PSSE_FACTS_DEVICE;
import static com.powsybl.psse.model.PsseVersion.Major.V35;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class FactsDeviceConverter extends AbstractConverter {

    FactsDeviceConverter(PsseFacts psseFactsDevice, ContainersMapping containerMapping, Network network, PsseVersion version, NodeBreakerImport nodeBreakerImport) {
        super(containerMapping, network);
        this.psseFactsDevice = Objects.requireNonNull(psseFactsDevice);
        this.version = version;
        this.nodeBreakerImport = Objects.requireNonNull(nodeBreakerImport);
    }

    void create() {
        if (psseFactsDevice.getJ() == 0) {
            createStatCom();
        }
    }

    private void createStatCom() {

        VoltageLevel voltageLevel = getNetwork().getVoltageLevel(getContainersMapping().getVoltageLevelId(psseFactsDevice.getI()));
        double maxReactivePower = psseFactsDevice.getShmx();
        double bMax = powerToShuntAdmittance(maxReactivePower, voltageLevel.getNominalV());

        StaticVarCompensatorAdder adder = voltageLevel.newStaticVarCompensator()
                .setId(getFactsDeviceId(psseFactsDevice.getName()))
                .setName(psseFactsDevice.getName())
                .setRegulationMode(StaticVarCompensator.RegulationMode.OFF)
                .setBmin(-bMax)
                .setBmax(bMax);

        String equipmentId = getNodeBreakerEquipmentId(PsseEquipmentType.PSSE_FACTS_DEVICE, psseFactsDevice.getI(), psseFactsDevice.getJ(), psseFactsDevice.getName());
        OptionalInt node = nodeBreakerImport.getNode(getNodeBreakerEquipmentIdBus(equipmentId, psseFactsDevice.getI()));
        if (node.isPresent()) {
            adder.setNode(node.getAsInt());
        } else {
            String busId = getBusId(psseFactsDevice.getI());
            adder.setConnectableBus(busId);
            adder.setBus(psseFactsDevice.getMode() == 0 ? null : busId);
        }
        adder.add();
    }

    void addControl() {
        StaticVarCompensator staticVarCompensator = getNetwork().getStaticVarCompensator(getFactsDeviceId(psseFactsDevice.getName()));

        // Add control only if staticVarCompensator has been created
        if (staticVarCompensator == null) {
            return;
        }

        Terminal regulatingTerminal = defineRegulatingTerminal(psseFactsDevice, getNetwork(), staticVarCompensator, version, nodeBreakerImport);
        // Discard control if the staticVarCompensator is controlling an isolated bus
        if (regulatingTerminal == null) {
            return;
        }

        double vnom = regulatingTerminal.getVoltageLevel().getNominalV();
        double targetQ = psseFactsDevice.getQdes();
        double targetV = psseFactsDevice.getVset() * vnom;
        StaticVarCompensator.RegulationMode regulationMode = StaticVarCompensator.RegulationMode.OFF;
        if (Double.isFinite(targetV) && targetV > 0.0) {
            regulationMode = StaticVarCompensator.RegulationMode.VOLTAGE;
        } else if (Double.isFinite(targetQ)) {
            regulationMode = StaticVarCompensator.RegulationMode.REACTIVE_POWER;
        }

        staticVarCompensator.setVoltageSetpoint(targetV)
                .setReactivePowerSetpoint(targetQ)
                .setRegulatingTerminal(regulatingTerminal)
                .setRegulationMode(regulationMode);
    }

    private static Terminal defineRegulatingTerminal(PsseFacts psseFactsDevice, Network network, StaticVarCompensator staticVarCompensator, PsseVersion version, NodeBreakerImport nodeBreakerImport) {
        Terminal regulatingTerminal = null;
        if (factsDeviceRegulatingBus(psseFactsDevice, version) == 0) {
            regulatingTerminal = staticVarCompensator.getTerminal();
        } else {
            String equipmentId = getNodeBreakerEquipmentId(PSSE_FACTS_DEVICE, psseFactsDevice.getI(), psseFactsDevice.getJ(), psseFactsDevice.getName());
            Optional<NodeBreakerImport.NodeBreakerControlNode> controlNode = nodeBreakerImport.getControlNode(getNodeBreakerEquipmentIdBus(equipmentId, factsDeviceRegulatingBus(psseFactsDevice, version)));
            if (controlNode.isPresent()) {
                regulatingTerminal = findTerminalNode(network, controlNode.get().getVoltageLevelId(), controlNode.get().getNode());
            } else {
                String regulatingBusId = getBusId(factsDeviceRegulatingBus(psseFactsDevice, version));
                Bus bus = network.getBusBreakerView().getBus(regulatingBusId);
                if (bus != null) {
                    regulatingTerminal = bus.getConnectedTerminalStream().findFirst().orElse(null);
                }
            }
        }
        if (regulatingTerminal == null) {
            LOGGER.warn("FactsDevice {}. Regulating terminal is not assigned as the bus is isolated", psseFactsDevice.getName());
        }
        return regulatingTerminal;
    }

    private static int factsDeviceRegulatingBus(PsseFacts factsDevice, PsseVersion psseVersion) {
        if (psseVersion.major() == V35) {
            return factsDevice.getFcreg();
        } else {
            return factsDevice.getRemot();
        }
    }

    private final PsseFacts psseFactsDevice;
    private final PsseVersion version;
    private final NodeBreakerImport nodeBreakerImport;
    private static final Logger LOGGER = LoggerFactory.getLogger(FactsDeviceConverter.class);
}
