/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import com.powsybl.iidm.network.HvdcLine.ConvertersMode;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.iidm.network.util.HvdcUtils;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.pf.*;

import java.util.*;
import java.util.stream.Stream;

import static com.powsybl.psse.converter.AbstractConverter.PsseEquipmentType.PSSE_VSC_DC_LINE;
import static com.powsybl.psse.model.PsseVersion.Major.V35;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class VscDcTransmissionLineConverter extends AbstractConverter {

    private static final double DEFAULT_MAXP_FACTOR = 1.2;

    VscDcTransmissionLineConverter(PsseVoltageSourceConverterDcTransmissionLine psseVscDcTransmissionLine, ContainersMapping containerMapping, Network network, PsseVersion version, NodeBreakerImport nodeBreakerImport) {
        super(containerMapping, network);
        this.psseVscDcTransmissionLine = Objects.requireNonNull(psseVscDcTransmissionLine);
        this.version = Objects.requireNonNull(version);
        this.nodeBreakerImport = nodeBreakerImport;
    }

    void create() {
        if (!getContainersMapping().isBusDefined(psseVscDcTransmissionLine.getConverter1().getIbus()) || !getContainersMapping().isBusDefined(psseVscDcTransmissionLine.getConverter2().getIbus())) {
            return;
        }

        PsseVoltageSourceConverter converter1 = psseVscDcTransmissionLine.getConverter1();
        PsseVoltageSourceConverter converter2 = psseVscDcTransmissionLine.getConverter2();
        double activePowerSetpoint = getVscDcTransmissionLineActivePowerSetpoint(converter1, converter2);
        ConvertersMode convertersMode = getConvertersMode(converter1, converter2);

        String busId1 = getBusId(converter1.getIbus());
        VoltageLevel voltageLevel1 = getNetwork().getVoltageLevel(getContainersMapping().getVoltageLevelId(converter1.getIbus()));
        VscConverterStationAdder adder1 = voltageLevel1.newVscConverterStation()
                .setId(getVscConverterId(getNetwork(), psseVscDcTransmissionLine, converter1))
                .setLossFactor((float) getLossFactor1(converter1, activePowerSetpoint, convertersMode))
                .setReactivePowerSetpoint(getReactiveSetpoint(converter1, activePowerSetpoint))
                .setVoltageRegulatorOn(false);

        String equipmentId1 = getNodeBreakerEquipmentId(PSSE_VSC_DC_LINE, converter1.getIbus(), psseVscDcTransmissionLine.getName());
        OptionalInt node1 = nodeBreakerImport.getNode(getNodeBreakerEquipmentIdBus(equipmentId1, converter1.getIbus()));
        if (node1.isPresent()) {
            adder1.setNode(node1.getAsInt());
        } else {
            adder1.setConnectableBus(busId1);
            adder1.setBus(psseVscDcTransmissionLine.getMdc() == 0 ? null : busId1);
        }
        VscConverterStation c1 = adder1.add();
        addReactiveLimits(c1, converter1);

        String busId2 = getBusId(converter2.getIbus());
        VoltageLevel voltageLevel2 = getNetwork().getVoltageLevel(getContainersMapping().getVoltageLevelId(converter2.getIbus()));
        VscConverterStationAdder adder2 = voltageLevel2.newVscConverterStation()
                .setId(getVscConverterId(getNetwork(), psseVscDcTransmissionLine, converter2))
                .setLossFactor((float) getLossFactor2(converter2, activePowerSetpoint, convertersMode))
                .setReactivePowerSetpoint(getReactiveSetpoint(converter2, activePowerSetpoint))
                .setVoltageRegulatorOn(false);

        String equipmentId2 = getNodeBreakerEquipmentId(PSSE_VSC_DC_LINE, converter2.getIbus(), psseVscDcTransmissionLine.getName());
        OptionalInt node2 = nodeBreakerImport.getNode(getNodeBreakerEquipmentIdBus(equipmentId2, converter2.getIbus()));
        if (node2.isPresent()) {
            adder2.setNode(node2.getAsInt());
        } else {
            adder2.setConnectableBus(busId2);
            adder2.setBus(psseVscDcTransmissionLine.getMdc() == 0 ? null : busId2);
        }
        VscConverterStation c2 = adder2.add();
        addReactiveLimits(c2, converter2);

        HvdcLineAdder adder = getNetwork().newHvdcLine()
            .setId(getVscDcTransmissionLineId(psseVscDcTransmissionLine.getName()))
            .setName(psseVscDcTransmissionLine.getName())
            .setR(psseVscDcTransmissionLine.getRdc())
            .setNominalV(getHvdcLineNominalV(voltageLevel1, voltageLevel2))
            .setActivePowerSetpoint(activePowerSetpoint)
            .setMaxP(getVscDcTransmissionLineMaxP(converter1, voltageLevel1.getNominalV(), converter2, voltageLevel2.getNominalV(), activePowerSetpoint))
            .setConvertersMode(convertersMode)
            .setConverterStationId1(c1.getId())
            .setConverterStationId2(c2.getId());
        adder.add();
    }

    private static double getLossFactor1(PsseVoltageSourceConverter converter1, double activePowerSetpoint, ConvertersMode convertersMode) {
        return convertersMode.equals(ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)
                ? getLossFactor(converter1, activePowerSetpoint, true)
                : getLossFactor(converter1, activePowerSetpoint, false);
    }

    private static double getLossFactor2(PsseVoltageSourceConverter converter2, double activePowerSetpoint, ConvertersMode convertersMode) {
        return convertersMode.equals(ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)
                ? getLossFactor(converter2, activePowerSetpoint, false)
                : getLossFactor(converter2, activePowerSetpoint, true);
    }

    private static double getLossFactor(PsseVoltageSourceConverter converter1, double activePowerSetpoint, boolean isRectifier) {
        if (isRectifier) {
            double pAC = activePowerSetpoint + converter1.getAloss() / 1000.0;
            return pAC > 0.0 ? (1.0 - activePowerSetpoint / pAC) * 100.0 : 0.0;
        } else {
            double pAC = activePowerSetpoint - converter1.getAloss() / 1000.0;
            return activePowerSetpoint > 0.0 ? (1.0 - pAC / activePowerSetpoint) * 100.0 : 0.0;
        }
    }

    private static double getReactiveSetpoint(PsseVoltageSourceConverter converter, double activePowerSetpoint) {
        if (converter.getType() == 1) {
            return 0.0;
        }
        double powerFactor = converter.getAcset();
        return powerFactor != 0.0 ? activePowerSetpoint * Math.sqrt(1 - powerFactor * powerFactor) / powerFactor : 0.0;
    }

    private static void addReactiveLimits(VscConverterStation c, PsseVoltageSourceConverter converter) {
        c.newMinMaxReactiveLimits()
                .setMaxQ(getMaxQ(converter))
                .setMinQ(getMinQ(converter))
                .add();
    }

    private static double getMaxQ(PsseVoltageSourceConverter converter) {
        return converter.getMode() == 1 ? converter.getMaxq() : 0.0;
    }

    private static double getMinQ(PsseVoltageSourceConverter converter) {
        return converter.getMode() == 1 ? converter.getMinq() : 0.0;
    }

    private static double getHvdcLineNominalV(VoltageLevel voltageLevel1, VoltageLevel voltageLevel2) {
        return Math.max(voltageLevel1.getNominalV(), voltageLevel2.getNominalV());
    }

    private static double getVscDcTransmissionLineActivePowerSetpoint(PsseVoltageSourceConverter converter1, PsseVoltageSourceConverter converter2) {
        if (converter1.getType() == 2) {
            return Math.abs(converter1.getDcset());
        } else if (converter2.getType() == 2) {
            return Math.abs(converter2.getDcset());
        } else {
            return 0.0;
        }
    }

    private static double getVscDcTransmissionLineMaxP(PsseVoltageSourceConverter converter1, double nominalV1, PsseVoltageSourceConverter converter2, double nominalV2, double activePowerSetpoint) {
        double maxP = Stream.of(converter1.getSmax(),
                        currentInAmpsToMw(converter1.getImax(), nominalV1),
                        converter2.getSmax(),
                        currentInAmpsToMw(converter2.getImax(), nominalV2)).max(Comparator.naturalOrder()).orElse(0.0);
        return maxP > 0.0 ? maxP : activePowerSetpoint * DEFAULT_MAXP_FACTOR;
    }

    private static ConvertersMode getConvertersMode(PsseVoltageSourceConverter converter1, PsseVoltageSourceConverter converter2) {
        if (converter1.getType() == 2) {
            return converter1.getDcset() > 0 ? ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER : ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER;
        } else if (converter2.getType() == 2) {
            return converter1.getDcset() > 0 ? ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER : ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER;
        } else {
            return ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER;
        }
    }

    public void addControl() {
        String id = getVscDcTransmissionLineId(psseVscDcTransmissionLine.getName());
        HvdcLine hvdcLine = getNetwork().getHvdcLine(id);

        if (hvdcLine == null) {
            return;
        }
        addControlConverter(getNetwork(), psseVscDcTransmissionLine.getConverter1(), (VscConverterStation) hvdcLine.getConverterStation1(), version, nodeBreakerImport);
        addControlConverter(getNetwork(), psseVscDcTransmissionLine.getConverter2(), (VscConverterStation) hvdcLine.getConverterStation2(), version, nodeBreakerImport);
    }

    private static void addControlConverter(Network network, PsseVoltageSourceConverter converter, VscConverterStation c, PsseVersion psseVersion, NodeBreakerImport nodeBreakerImport) {
        Terminal regulatingTerminal = findRegulatingTerminal(network, converter, c, nodeBreakerImport, psseVersion);
        c.setRegulatingTerminal(regulatingTerminal)
                .setVoltageSetpoint(findTargetVpu(converter) * regulatingTerminal.getVoltageLevel().getNominalV())
                .setVoltageRegulatorOn(findIsRegulatingOn(converter));
    }

    private static Terminal findRegulatingTerminal(Network network, PsseVoltageSourceConverter converter, VscConverterStation c, NodeBreakerImport nodeBreakerImport, PsseVersion psseVersion) {
        Terminal regulatingTerminal = null;
        Optional<NodeBreakerImport.ControlR> control = nodeBreakerImport.getControl(vscDcTransmissionLineRegulatingBus(converter, psseVersion));
        if (control.isPresent()) {
            regulatingTerminal = findTerminalNode(network, control.get().voltageLevelId(), control.get().node());
        } else {
            String regulatingBusId = getBusId(vscDcTransmissionLineRegulatingBus(converter, psseVersion));
            Bus bus = network.getBusBreakerView().getBus(regulatingBusId);
            if (bus != null) {
                regulatingTerminal = bus.getConnectedTerminalStream().findFirst().orElse(null);
            }
        }
        return regulatingTerminal != null ? regulatingTerminal : c.getTerminal();
    }

    private static int vscDcTransmissionLineRegulatingBus(PsseVoltageSourceConverter converter, PsseVersion psseVersion) {
        if (psseVersion.major() == V35) {
            return converter.getVsreg();
        } else {
            return converter.getRemot();
        }
    }

    private static double findTargetVpu(PsseVoltageSourceConverter converter) {
        return converter.getMode() == 1 && isVoltageSetpointValid(converter.getAcset()) ? converter.getAcset() : 1.0;
    }

    private static boolean isVoltageSetpointValid(double targetV) {
        return Double.isFinite(targetV) && targetV > 0.0;
    }

    private static boolean findIsRegulatingOn(PsseVoltageSourceConverter converter) {
        return converter.getMode() == 1;
    }

    static void create(Network network, PssePowerFlowModel psseModel, ContextExport contextExport) {
        PsseVersion version = PsseVersion.fromRevision(psseModel.getCaseIdentification().getRev());
        network.getHvdcLines().forEach(hvdcLine -> {
            if (isVscDcTransmissionLine(hvdcLine)) {
                psseModel.addVoltageSourceConverterDcTransmissionLines(Collections.singletonList(createVscDcTransmissionLine(hvdcLine, version, contextExport)));
            }
        });
        psseModel.replaceAllVoltageSourceConverterDcTransmissionLines(psseModel.getVoltageSourceConverterDcTransmissionLines().stream().sorted(Comparator.comparing(PsseVoltageSourceConverterDcTransmissionLine::getName)).toList());
    }

    private static PsseVoltageSourceConverterDcTransmissionLine createVscDcTransmissionLine(HvdcLine hvdcLine, PsseVersion version, ContextExport contextExport) {
        PsseVoltageSourceConverterDcTransmissionLine vscDcTransmissionLine = new PsseVoltageSourceConverterDcTransmissionLine();
        vscDcTransmissionLine.setName(extractVscDcTransmissionLineName(hvdcLine.getId()));
        vscDcTransmissionLine.setMdc(findControlMode(hvdcLine, contextExport));
        vscDcTransmissionLine.setRdc(hvdcLine.getR());
        vscDcTransmissionLine.setOwnership(createDefaultOwnership());

        vscDcTransmissionLine.setConverter1(createConverter((VscConverterStation) hvdcLine.getConverterStation1(), version, contextExport));
        vscDcTransmissionLine.setConverter2(createConverter((VscConverterStation) hvdcLine.getConverterStation2(), version, contextExport));
        return vscDcTransmissionLine;
    }

    private static PsseVoltageSourceConverter createConverter(VscConverterStation vscConverter, PsseVersion version, ContextExport contextExport) {
        PsseVoltageSourceConverter psseConverter = createDefaultConverter();
        int busI = getTerminalBusI(vscConverter.getTerminal(), contextExport);
        int regulatingBus = getRegulatingTerminalBusI(vscConverter.getRegulatingTerminal(), busI, vscDcTransmissionLineRegulatingBus(psseConverter, version), contextExport);
        double converterTargetP = HvdcUtils.getConverterStationTargetP(vscConverter);

        psseConverter.setIbus(busI);
        psseConverter.setType(2);
        psseConverter.setMode(getMode(vscConverter));
        psseConverter.setDcset(converterTargetP);
        psseConverter.setAcset(findAcset(vscConverter, getMode(vscConverter), converterTargetP));
        psseConverter.setAloss(findALosses(vscConverter.getLossFactor(), converterTargetP));
        psseConverter.setMaxq(checkAndFixMaxQ(vscConverter.getReactiveLimits().getMaxQ(converterTargetP)));
        psseConverter.setMinq(checkAndFixMinQ(vscConverter.getReactiveLimits().getMinQ(converterTargetP)));
        psseConverter.setVsreg(regulatingBus);
        psseConverter.setNreg(getRegulatingTerminalNode(vscConverter.getRegulatingTerminal(), contextExport));

        return psseConverter;
    }

    private static int getMode(VscConverterStation vscConverter) {
        return vscConverter.isVoltageRegulatorOn() ? 1 : 2;
    }

    private static double findAcset(VscConverterStation vscConverter, int mode, double targetP) {
        if (mode == 1) {
            double targetV = vscConverter.getVoltageSetpoint();
            return Double.isFinite(targetV) && targetV > 0.0 ? targetV / vscConverter.getRegulatingTerminal().getVoltageLevel().getNominalV() : 1.0;
        } else {
            double targetQ = vscConverter.getReactivePowerSetpoint();
            return Double.isFinite(targetP) && targetP != 0.0 && Double.isFinite(targetQ) && targetQ != 0.0 ? targetP / Math.sqrt(targetP * targetP + targetQ * targetQ) : 1.0;
        }
    }

    private static double findALosses(double lossFactor, double converterTargetP) {
        return converterTargetP != 0.0 ? 1000.0 * lossFactor * converterTargetP / 100.0 : 0.0;
    }

    private static double checkAndFixMaxQ(double maxQ) {
        return Double.isNaN(maxQ) ? 9999.0 : maxQ;
    }

    private static double checkAndFixMinQ(double minQ) {
        return Double.isNaN(minQ) ? -9999.0 : minQ;
    }

    private static PsseVoltageSourceConverter createDefaultConverter() {
        PsseVoltageSourceConverter converter = new PsseVoltageSourceConverter();
        converter.setIbus(0);
        converter.setType(1);
        converter.setMode(1);
        converter.setDcset(0.0);
        converter.setAcset(1.0);
        converter.setAloss(0.0);
        converter.setBloss(0.0);
        converter.setMinloss(0.0);
        converter.setSmax(0.0);
        converter.setImax(0.0);
        converter.setPwf(1.0);
        converter.setMaxq(9999.0);
        converter.setMinq(-9999.0);
        converter.setVsreg(0);
        converter.setNreg(0);
        converter.setRmpct(100.0);
        return converter;
    }

    static void update(Network network, PssePowerFlowModel psseModel) {
        psseModel.getVoltageSourceConverterDcTransmissionLines().forEach(psseVscDcTransmissionLine -> {
            String hvdcId = getVscDcTransmissionLineId(psseVscDcTransmissionLine.getName());
            HvdcLine hvdcLine = network.getHvdcLine(hvdcId);

            if (hvdcLine == null) {
                psseVscDcTransmissionLine.setMdc(0);
            } else {
                psseVscDcTransmissionLine.setMdc(findUpdatedControlMode(hvdcLine));
            }
        });
    }

    private static int findControlMode(HvdcLine hvdcLine, ContextExport contextExport) {
        return getStatus(hvdcLine.getConverterStation1().getTerminal(), contextExport) == 1
                && getStatus(hvdcLine.getConverterStation2().getTerminal(), contextExport) == 1 ? 1 : 0;
    }

    private static int findUpdatedControlMode(HvdcLine hvdcLine) {
        return getUpdatedStatus(hvdcLine.getConverterStation1().getTerminal()) == 1
                && getUpdatedStatus(hvdcLine.getConverterStation2().getTerminal()) == 1 ? 1 : 0;
    }

    private final PsseVoltageSourceConverterDcTransmissionLine psseVscDcTransmissionLine;
    private final PsseVersion version;
    private final NodeBreakerImport nodeBreakerImport;
}
