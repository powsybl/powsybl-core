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
import com.powsybl.psse.model.pf.PssePowerFlowModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        if (!getContainersMapping().isBusDefined(psseFactsDevice.getI())) {
            return;
        }
        if (isStatCom(psseFactsDevice)) {
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
                .setRegulating(false)
                .setRegulationMode(StaticVarCompensator.RegulationMode.REACTIVE_POWER)
                .setBmin(-bMax)
                .setBmax(bMax);

        String equipmentId = getNodeBreakerEquipmentId(PsseEquipmentType.PSSE_FACTS_DEVICE, psseFactsDevice.getI(), psseFactsDevice.getJ(), psseFactsDevice.getName());
        OptionalInt node = nodeBreakerImport.getNode(getNodeBreakerEquipmentIdBus(equipmentId, psseFactsDevice.getI(), 0, 0, psseFactsDevice.getI(), "I"));
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
        boolean isRegulating = false;
        StaticVarCompensator.RegulationMode regulationMode = StaticVarCompensator.RegulationMode.REACTIVE_POWER;
        if (Double.isFinite(targetV) && targetV > 0.0) {
            regulationMode = StaticVarCompensator.RegulationMode.VOLTAGE;
            isRegulating = true;
        } else if (Double.isFinite(targetQ)) {
            isRegulating = true;
        }

        staticVarCompensator.setVoltageSetpoint(targetV)
                .setReactivePowerSetpoint(targetQ)
                .setRegulatingTerminal(regulatingTerminal)
                .setRegulationMode(regulationMode)
                .setRegulating(isRegulating);
    }

    private static Terminal defineRegulatingTerminal(PsseFacts psseFactsDevice, Network network, StaticVarCompensator staticVarCompensator, PsseVersion version, NodeBreakerImport nodeBreakerImport) {
        Terminal regulatingTerminal = null;
        if (factsDeviceRegulatingBus(psseFactsDevice, version) == 0) {
            regulatingTerminal = staticVarCompensator.getTerminal();
        } else {
            Optional<NodeBreakerImport.ControlR> control = nodeBreakerImport.getControl(factsDeviceRegulatingBus(psseFactsDevice, version));
            if (control.isPresent()) {
                regulatingTerminal = findTerminalNode(network, control.get().voltageLevelId(), control.get().node());
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

    static void create(Network network, PssePowerFlowModel psseModel, ContextExport contextExport) {
        network.getStaticVarCompensators().forEach(staticVarCompensator -> psseModel.addFacts(Collections.singletonList(createFactsDevice(staticVarCompensator, contextExport))));
        psseModel.replaceAllFacts(psseModel.getFacts().stream().sorted(Comparator.comparing(PsseFacts::getName)).toList());
    }

    private static PsseFacts createFactsDevice(StaticVarCompensator staticVarCompensator, ContextExport contextExport) {
        PsseFacts psseFactsDevice = createDefaultFactsDevice();
        int busI = getTerminalBusI(staticVarCompensator.getTerminal(), contextExport);
        double maxReactivePower = shuntAdmittanceToPower(staticVarCompensator.getBmax(), staticVarCompensator.getTerminal().getVoltageLevel().getNominalV());

        psseFactsDevice.setName(extractFactsDeviceName(staticVarCompensator.getId()));
        psseFactsDevice.setI(busI);
        psseFactsDevice.setMode(getStatus(staticVarCompensator.getTerminal(), contextExport));
        findTargetQ(staticVarCompensator).ifPresent(psseFactsDevice::setQdes);
        findTargetV(staticVarCompensator).ifPresent(psseFactsDevice::setVset);
        psseFactsDevice.setShmx(maxReactivePower);

        psseFactsDevice.setFcreg(getRegulatingTerminalBusI(staticVarCompensator.getRegulatingTerminal(), busI, psseFactsDevice.getFcreg(), contextExport));
        psseFactsDevice.setNreg(getRegulatingTerminalNode(staticVarCompensator.getRegulatingTerminal(), contextExport));
        return psseFactsDevice;
    }

    private static PsseFacts createDefaultFactsDevice() {
        PsseFacts psseFactsDevice = new PsseFacts();
        psseFactsDevice.setName("");
        psseFactsDevice.setI(0);
        psseFactsDevice.setJ(0);
        psseFactsDevice.setMode(1);
        psseFactsDevice.setPdes(0.0);
        psseFactsDevice.setQdes(0.0);
        psseFactsDevice.setVset(1.0);
        psseFactsDevice.setShmx(9999.0);
        psseFactsDevice.setTrmx(9999.0);
        psseFactsDevice.setVtmn(0.9);
        psseFactsDevice.setVtmx(1.1);
        psseFactsDevice.setVsmx(1.0);
        psseFactsDevice.setImx(0.0);
        psseFactsDevice.setLinx(0.05);
        psseFactsDevice.setRmpct(100.0);
        psseFactsDevice.setOwner(1);
        psseFactsDevice.setSet1(0.0);
        psseFactsDevice.setSet2(0.0);
        psseFactsDevice.setVsref(0);
        psseFactsDevice.setFcreg(0);
        psseFactsDevice.setNreg(0);
        psseFactsDevice.setMname("");
        return psseFactsDevice;
    }

    static void update(Network network, PssePowerFlowModel psseModel) {
        psseModel.getFacts().forEach(psseFactsDevice -> {
            String factsDeviceName = getFactsDeviceId(psseFactsDevice.getName());
            StaticVarCompensator staticVarCompensator = network.getStaticVarCompensator(factsDeviceName);
            if (staticVarCompensator == null) {
                psseFactsDevice.setMode(0);
            } else if (isStatCom(psseFactsDevice)) {
                psseFactsDevice.setMode(getUpdatedStatus(staticVarCompensator.getTerminal()));
            } else {
                psseFactsDevice.setMode(getUpdatedStatus(staticVarCompensator.getTerminal()));
                findTargetQ(staticVarCompensator).ifPresent(psseFactsDevice::setQdes);
                findTargetV(staticVarCompensator).ifPresent(psseFactsDevice::setVset);
            }
        });
    }

    private static OptionalDouble findTargetQ(StaticVarCompensator staticVarCompensator) {
        return Double.isFinite(staticVarCompensator.getReactivePowerSetpoint()) ? OptionalDouble.of(staticVarCompensator.getReactivePowerSetpoint()) : OptionalDouble.empty();
    }

    private static OptionalDouble findTargetV(StaticVarCompensator staticVarCompensator) {
        return staticVarCompensator.getRegulatingTerminal() != null ? OptionalDouble.of(staticVarCompensator.getVoltageSetpoint() / staticVarCompensator.getRegulatingTerminal().getVoltageLevel().getNominalV()) : OptionalDouble.empty();
    }

    private static boolean isStatCom(PsseFacts psseFactsDevice) {
        return psseFactsDevice.getJ() == 0;
    }

    private final PsseFacts psseFactsDevice;
    private final PsseVersion version;
    private final NodeBreakerImport nodeBreakerImport;
    private static final Logger LOGGER = LoggerFactory.getLogger(FactsDeviceConverter.class);
}
