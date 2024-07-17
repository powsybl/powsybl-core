/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import java.util.*;
import java.util.stream.Collectors;

import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.pf.PssePowerFlowModel;
import com.powsybl.psse.model.pf.PsseSwitchedShunt;
import static com.powsybl.psse.converter.AbstractConverter.PsseEquipmentType.PSSE_SWITCHED_SHUNT;
import static com.powsybl.psse.model.PsseVersion.Major.V35;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class SwitchedShuntCompensatorConverter extends AbstractConverter {

    SwitchedShuntCompensatorConverter(PsseSwitchedShunt psseSwitchedShunt, ContainersMapping containerMapping, Network network, PsseVersion version, NodeBreakerImport nodeBreakerImport) {
        super(containerMapping, network);
        this.psseSwitchedShunt = Objects.requireNonNull(psseSwitchedShunt);
        this.version = Objects.requireNonNull(version);
        this.nodeBreakerImport = Objects.requireNonNull(nodeBreakerImport);
    }

    void create() {
        List<ShuntBlock> shuntBlocks = defineShuntBlocks(psseSwitchedShunt, version);
        if (shuntBlocks.isEmpty()) {
            return;
        }

        String id = defineShuntId(psseSwitchedShunt, version);
        VoltageLevel voltageLevel = getNetwork().getVoltageLevel(getContainersMapping().getVoltageLevelId(psseSwitchedShunt.getI()));

        ShuntCompensatorAdder adder = voltageLevel.newShuntCompensator()
            .setId(getSwitchedShuntId(psseSwitchedShunt.getI(), id))
            .setSectionCount(defineSectionCount(psseSwitchedShunt.getBinit(), shuntBlocks));

        String equipmentId = getNodeBreakerEquipmentId(PSSE_SWITCHED_SHUNT, psseSwitchedShunt.getI(), defineShuntId(psseSwitchedShunt, version));
        OptionalInt node = nodeBreakerImport.getNode(getNodeBreakerEquipmentIdBus(equipmentId, psseSwitchedShunt.getI()));
        if (node.isPresent()) {
            adder.setNode(node.getAsInt());
        } else {
            String busId = getBusId(psseSwitchedShunt.getI());
            adder.setConnectableBus(busId);
            adder.setBus(psseSwitchedShunt.getStat() == 1 ? busId : null);
        }

        ShuntCompensatorNonLinearModelAdder modelAdder = adder.newNonLinearModel();
        shuntBlocks.forEach(shuntBlock -> {
            for (int i = 0; i < shuntBlock.getN(); i++) {
                modelAdder.beginSection()
                    .setG(0.0)
                    .setB(powerToShuntAdmittance(shuntBlock.getB(), voltageLevel.getNominalV()))
                    .endSection();
            }
        });
        modelAdder.add();
        adder.add();
    }

    void addControl() {
        String id = defineShuntId(psseSwitchedShunt, version);
        ShuntCompensator shunt = getNetwork().getShuntCompensator(getSwitchedShuntId(psseSwitchedShunt.getI(), id));

        // Add control only if shunt has been created
        if (shunt == null) {
            return;
        }

        Terminal regulatingTerminal = defineRegulatingTerminal(psseSwitchedShunt, getNetwork(), shunt, version, nodeBreakerImport);
        // Discard control if the switchedShunt is controlling an isolated bus
        if (regulatingTerminal == null) {
            return;
        }
        if (!isControllingVoltage(psseSwitchedShunt)) {
            return;
        }
        boolean psseVoltageRegulatorOn = true;
        double vnom = regulatingTerminal.getVoltageLevel().getNominalV();
        double vLow = psseSwitchedShunt.getVswlo() * vnom;
        double vHigh = psseSwitchedShunt.getVswhi() * vnom;
        double targetV = 0.5 * (vLow + vHigh);
        boolean voltageRegulatorOn = false;
        double targetDeadband = 0.0;
        if (targetV != 0.0) {
            targetDeadband = vHigh - vLow;
            voltageRegulatorOn = psseVoltageRegulatorOn;
        }

        shunt.setTargetV(targetV)
            .setTargetDeadband(targetDeadband)
            .setVoltageRegulatorOn(voltageRegulatorOn)
            .setRegulatingTerminal(regulatingTerminal);
    }

    private static boolean isControllingVoltage(PsseSwitchedShunt psseSwitchedShunt) {
        return psseSwitchedShunt.getModsw() == 1 || psseSwitchedShunt.getModsw() == 2;
    }

    // Nreg (version 35) is not yet considered
    private static Terminal defineRegulatingTerminal(PsseSwitchedShunt psseSwitchedShunt, Network network, ShuntCompensator shunt, PsseVersion version, NodeBreakerImport nodeBreakerImport) {
        Terminal regulatingTerminal = null;
        if (switchedShuntRegulatingBus(psseSwitchedShunt, version) == 0) {
            regulatingTerminal = shunt.getTerminal();
        } else {
            String equipmentId = getNodeBreakerEquipmentId(PSSE_SWITCHED_SHUNT, psseSwitchedShunt.getI(), defineShuntId(psseSwitchedShunt, version));
            Optional<NodeBreakerImport.NodeBreakerControlNode> controlNode = nodeBreakerImport.getControlNode(getNodeBreakerEquipmentIdBus(equipmentId, switchedShuntRegulatingBus(psseSwitchedShunt, version)));
            if (controlNode.isPresent()) {
                regulatingTerminal = findTerminalNode(network, controlNode.get().getVoltageLevelId(), controlNode.get().getNode());
            } else {
                String regulatingBusId = getBusId(switchedShuntRegulatingBus(psseSwitchedShunt, version));
                Bus bus = network.getBusBreakerView().getBus(regulatingBusId);
                if (bus != null) {
                    regulatingTerminal = bus.getConnectedTerminalStream().findFirst().orElse(null);
                }
            }
        }
        if (regulatingTerminal == null) {
            String shuntId = defineShuntId(psseSwitchedShunt, version);
            LOGGER.warn("SwitchedShunt {}. Regulating terminal is not assigned as the bus is isolated", shuntId);
        }
        return regulatingTerminal;
    }

    private static int switchedShuntRegulatingBus(PsseSwitchedShunt switchedShunt, PsseVersion psseVersion) {
        if (psseVersion.major() == V35) {
            return switchedShunt.getSwreg();
        } else {
            return switchedShunt.getSwrem();
        }
    }

    private static void setSwitchedShuntRegulatingBus(PsseSwitchedShunt switchedShunt, PsseVersion psseVersion, int regulatingBus) {
        if (psseVersion.major() == V35) {
            switchedShunt.setSwreg(regulatingBus);
        } else {
            switchedShunt.setSwrem(regulatingBus);
        }
    }

    private static int defineSectionCount(double binit, List<ShuntBlock> shuntBlocks) {
        double maxDistance = Double.MAX_VALUE;
        int sectionCount = 0;
        for (int i = 0; i < shuntBlocks.size(); i++) {
            double d = Math.abs(binit - shuntBlocks.get(i).getB());
            if (d < maxDistance) {
                maxDistance = d;
                sectionCount = i + 1; // index + 1 (count) is expected as sectionCount
            }
        }
        return sectionCount;
    }

// IIDM only considers consecutive sections
    private static List<ShuntBlock> defineShuntBlocks(PsseSwitchedShunt psseSwitchedShunt, PsseVersion version) {
        List<ShuntBlock> psseBlocks = collectShuntBlocks(psseSwitchedShunt, version);
        List<ShuntBlock> psseReactorBlocks = psseBlocks.stream().filter(sb -> sb.getB() < 0.0)
            .collect(Collectors.toList());
        List<ShuntBlock> psseCapacitorBlocks = psseBlocks.stream().filter(sb -> sb.getB() > 0.0)
            .collect(Collectors.toList());

        // In that case we do not consider any switched combination
        // blocks are sorted and switched on in input order
        // When Adjm is zero the input order is considered
        if (psseSwitchedShunt.getAdjm() == 1) {
            psseReactorBlocks.sort(Comparator.comparing(ShuntBlock::getB).reversed());
            psseCapacitorBlocks.sort(Comparator.comparing(ShuntBlock::getB));

            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Switched combination not exactly supported ({})",
                        getSwitchedShuntId(psseSwitchedShunt.getI(), defineShuntId(psseSwitchedShunt, version)));
            }
        }

        double bAdd = 0.0;
        List<ShuntBlock> shuntBlocks = new ArrayList<>();
        bAdd = addShuntBlocks(psseReactorBlocks, bAdd, shuntBlocks);

        if (psseSwitchedShunt.getAdjm() == 1) {
            bAdd = 0.0;
        }

        addShuntBlocks(psseCapacitorBlocks, bAdd, shuntBlocks);

        // Add the zero block, shunt disconnected
        shuntBlocks.add(new ShuntBlock(1, 1, 0.0));

        shuntBlocks.sort(Comparator.comparing(ShuntBlock::getB));

        return shuntBlocks;
    }

    private static double addShuntBlocks(List<ShuntBlock> psseShuntBlocks, double bAddInitial, List<ShuntBlock> shuntBlocks) {
        double bAdd = bAddInitial;
        if (!psseShuntBlocks.isEmpty()) {
            for (ShuntBlock psseCapacitorBlock : psseShuntBlocks) {
                for (int j = 0; j < psseCapacitorBlock.getN(); j++) {
                    bAdd = bAdd + psseCapacitorBlock.getB();
                    shuntBlocks.add(new ShuntBlock(1, 1, bAdd));
                }
            }
        }
        return bAdd;
    }

// defined blocks can be reactors (< 0) or / and capacitors ( > 0)
    private static List<ShuntBlock> collectShuntBlocks(PsseSwitchedShunt psseSwitchedShunt, PsseVersion version) {
        List<ShuntBlock> shuntBlocks = new ArrayList<>();
        if (version.major() == V35) {
            addShuntBlock(shuntBlocks, psseSwitchedShunt.getS1(), psseSwitchedShunt.getN1(), psseSwitchedShunt.getB1());
            addShuntBlock(shuntBlocks, psseSwitchedShunt.getS2(), psseSwitchedShunt.getN2(), psseSwitchedShunt.getB2());
            addShuntBlock(shuntBlocks, psseSwitchedShunt.getS3(), psseSwitchedShunt.getN3(), psseSwitchedShunt.getB3());
            addShuntBlock(shuntBlocks, psseSwitchedShunt.getS4(), psseSwitchedShunt.getN4(), psseSwitchedShunt.getB4());
            addShuntBlock(shuntBlocks, psseSwitchedShunt.getS5(), psseSwitchedShunt.getN5(), psseSwitchedShunt.getB5());
            addShuntBlock(shuntBlocks, psseSwitchedShunt.getS6(), psseSwitchedShunt.getN6(), psseSwitchedShunt.getB6());
            addShuntBlock(shuntBlocks, psseSwitchedShunt.getS7(), psseSwitchedShunt.getN7(), psseSwitchedShunt.getB7());
            addShuntBlock(shuntBlocks, psseSwitchedShunt.getS8(), psseSwitchedShunt.getN8(), psseSwitchedShunt.getB8());
        } else {
            addShuntBlock(shuntBlocks, 1, psseSwitchedShunt.getN1(), psseSwitchedShunt.getB1());
            addShuntBlock(shuntBlocks, 1, psseSwitchedShunt.getN2(), psseSwitchedShunt.getB2());
            addShuntBlock(shuntBlocks, 1, psseSwitchedShunt.getN3(), psseSwitchedShunt.getB3());
            addShuntBlock(shuntBlocks, 1, psseSwitchedShunt.getN4(), psseSwitchedShunt.getB4());
            addShuntBlock(shuntBlocks, 1, psseSwitchedShunt.getN5(), psseSwitchedShunt.getB5());
            addShuntBlock(shuntBlocks, 1, psseSwitchedShunt.getN6(), psseSwitchedShunt.getB6());
            addShuntBlock(shuntBlocks, 1, psseSwitchedShunt.getN7(), psseSwitchedShunt.getB7());
            addShuntBlock(shuntBlocks, 1, psseSwitchedShunt.getN8(), psseSwitchedShunt.getB8());
        }
        return shuntBlocks;
    }

    // Only in-service blocks are included (in-service s = 1 and out-of-service s = 0)
    private static void addShuntBlock(List<ShuntBlock> shuntBlocks, int s, int n, double b) {
        if (s == 0 || n == 0 || b == 0.0) {
            return;
        }
        shuntBlocks.add(new ShuntBlock(s, n, b));
    }

    static class ShuntBlock {
        int s;
        int n;
        double b;

        ShuntBlock(int s, int n, double b) {
            this.s = s;
            this.n = n;
            this.b = b;
        }

        int getS() {
            return s;
        }

        int getN() {
            return n;
        }

        double getB() {
            return b;
        }
    }

    private static String defineShuntId(PsseSwitchedShunt psseSwitchedShunt, PsseVersion version) {
        if (version.major() == V35) {
            return psseSwitchedShunt.getId();
        } else {
            return "1";
        }
    }

    static void updateAndCreateSwitchedShunts(Network network, PssePowerFlowModel psseModel, ContextExport contextExport) {
        Map<String, PsseSwitchedShunt> shuntCompensatorsToPsseSwitchedShunt = new HashMap<>();
        psseModel.getSwitchedShunts().forEach(psseSwitchedShunt -> shuntCompensatorsToPsseSwitchedShunt.put(getSwitchedShuntId(psseSwitchedShunt.getI(), psseSwitchedShunt.getId()), psseSwitchedShunt));

        PsseVersion version = PsseVersion.fromRevision(psseModel.getCaseIdentification().getRev());
        network.getShuntCompensators().forEach(shuntCompensator -> {
            if (!isFixedShunt(shuntCompensator)) {
                if (shuntCompensatorsToPsseSwitchedShunt.containsKey(shuntCompensator.getId())) {
                    updateSwitchedShunt(shuntCompensator, shuntCompensatorsToPsseSwitchedShunt.get(shuntCompensator.getId()), version, contextExport);
                } else {
                    psseModel.addSwitchedShunts(Collections.singletonList(createSwitchedShunt(shuntCompensator, version, contextExport)));
                }
            }
        });
        psseModel.replaceAllSwitchedShunts(psseModel.getSwitchedShunts().stream().sorted(Comparator.comparingInt(PsseSwitchedShunt::getI).thenComparing(PsseSwitchedShunt::getId)).toList());
    }

    static void updateSwitchedShunt(ShuntCompensator shuntCompensator, PsseSwitchedShunt psseSwitchedShunt, PsseVersion version, ContextExport contextExport) {
        int bus = getTerminalBusI(shuntCompensator.getTerminal(), contextExport);
        int regulatingBus = getRegulatingTerminalBusI(shuntCompensator.getRegulatingTerminal(), bus, switchedShuntRegulatingBus(psseSwitchedShunt, version), contextExport);

        psseSwitchedShunt.setStat(getStatus(shuntCompensator));
        psseSwitchedShunt.setBinit(getQ(shuntCompensator));
        psseSwitchedShunt.setI(bus);
        setSwitchedShuntRegulatingBus(psseSwitchedShunt, version, regulatingBus);
    }

    private static double getQ(ShuntCompensator switchedShunt) {
        return shuntAdmittanceToPower(switchedShunt.getB(), switchedShunt.getTerminal().getVoltageLevel().getNominalV());
    }

    static PsseSwitchedShunt createSwitchedShunt(ShuntCompensator shuntCompensator, PsseVersion version, ContextExport contextExport) {
        PsseSwitchedShunt psseSwitchedShunt = new PsseSwitchedShunt();

        int busI = getTerminalBusI(shuntCompensator.getTerminal(), contextExport);
        int regulatingBus = getRegulatingTerminalBusI(shuntCompensator.getRegulatingTerminal(), busI, switchedShuntRegulatingBus(psseSwitchedShunt, version), contextExport);
        psseSwitchedShunt.setI(busI);
        psseSwitchedShunt.setId(contextExport.getEquipmentCkt(shuntCompensator.getId(), IdentifiableType.SHUNT_COMPENSATOR, busI));
        psseSwitchedShunt.setModsw(getModsw(shuntCompensator));
        psseSwitchedShunt.setAdjm(0);
        psseSwitchedShunt.setStat(getStatus(shuntCompensator));
        psseSwitchedShunt.setVswhi(getVswhi(shuntCompensator));
        psseSwitchedShunt.setVswlo(getVswlo(shuntCompensator));
        psseSwitchedShunt.setSwreg(regulatingBus);
        psseSwitchedShunt.setNreg(getRegulatingTerminalNode(shuntCompensator.getRegulatingTerminal(), contextExport));
        psseSwitchedShunt.setRmpct(100.0);
        psseSwitchedShunt.setRmidnt(" ");
        psseSwitchedShunt.setBinit(shuntAdmittanceToPower(shuntCompensator.getB(), shuntCompensator.getTerminal().getVoltageLevel().getNominalV()));

        setShuntBlocks(shuntCompensator, psseSwitchedShunt);
        return psseSwitchedShunt;
    }

    private static int getModsw(ShuntCompensator shuntCompensator) {
        return shuntCompensator.isVoltageRegulatorOn() ? 1 : 0;
    }

    private static double getVswhi(ShuntCompensator shuntCompensator) {
        double targetV = shuntCompensator.getTargetV() + shuntCompensator.getTargetDeadband() * 0.5;
        double nominalV = getRegulatingTerminalNominalV(shuntCompensator);
        return Double.isFinite(targetV) && Double.isFinite(nominalV) && targetV > 0 && nominalV > 0 ? targetV / nominalV : 1.0;
    }

    private static double getVswlo(ShuntCompensator shuntCompensator) {
        double targetV = shuntCompensator.getTargetV() - shuntCompensator.getTargetDeadband() * 0.5;
        double nominalV = getRegulatingTerminalNominalV(shuntCompensator);

        return Double.isFinite(targetV) && Double.isFinite(nominalV) && targetV > 0 && nominalV > 0 ? targetV / nominalV : 1.0;
    }

    private static double getRegulatingTerminalNominalV(ShuntCompensator shuntCompensator) {
        return shuntCompensator.getRegulatingTerminal() != null ? shuntCompensator.getRegulatingTerminal().getVoltageLevel().getNominalV() : shuntCompensator.getTerminal().getVoltageLevel().getNominalV();
    }

    private static void setShuntBlocks(ShuntCompensator shuntCompensator, PsseSwitchedShunt psseSwitchedShunt) {
        if (shuntCompensator.getModelType().equals(ShuntCompensatorModelType.LINEAR)) {
            ShuntCompensatorLinearModel linearModel = (ShuntCompensatorLinearModel) shuntCompensator.getModel();
            setShuntBlocksForLinearModel(linearModel, shuntCompensator.getMaximumSectionCount(), shuntCompensator.getTerminal().getVoltageLevel().getNominalV(), psseSwitchedShunt);
        } else {
            ShuntCompensatorNonLinearModel nonLinearModel = (ShuntCompensatorNonLinearModel) shuntCompensator.getModel();
            setShuntBlocksForNonLinearModel(nonLinearModel, shuntCompensator.getTerminal().getVoltageLevel().getNominalV(), psseSwitchedShunt);
        }
    }

    private static void setShuntBlocksForLinearModel(ShuntCompensatorLinearModel linearModel, int maximumSectionCount, double nominalV, PsseSwitchedShunt psseSwitchedShunt) {
        setDefaultShuntBlocks(psseSwitchedShunt);
        psseSwitchedShunt.setN1(maximumSectionCount);
        psseSwitchedShunt.setB1(shuntAdmittanceToPower(linearModel.getBPerSection(), nominalV));
    }

    private static void setShuntBlocksForNonLinearModel(ShuntCompensatorNonLinearModel nonLinearModel, double nominalV, PsseSwitchedShunt psseSwitchedShunt) {
        List<ShuntCompensatorNonLinearModel.Section> sections = nonLinearModel.getAllSections().stream().filter(section -> section.getB() != 0.0).toList();
        psseSwitchedShunt.setN1(getN(sections, 0));
        psseSwitchedShunt.setB1(shuntAdmittanceToPower(getB(sections, 0), nominalV));
        psseSwitchedShunt.setN2(getN(sections, 1));
        psseSwitchedShunt.setB2(shuntAdmittanceToPower(getB(sections, 1), nominalV));
        psseSwitchedShunt.setN3(getN(sections, 2));
        psseSwitchedShunt.setB3(shuntAdmittanceToPower(getB(sections, 2), nominalV));
        psseSwitchedShunt.setN4(getN(sections, 3));
        psseSwitchedShunt.setB4(shuntAdmittanceToPower(getB(sections, 3), nominalV));
        psseSwitchedShunt.setN5(getN(sections, 4));
        psseSwitchedShunt.setB5(shuntAdmittanceToPower(getB(sections, 4), nominalV));
        psseSwitchedShunt.setN6(getN(sections, 5));
        psseSwitchedShunt.setB6(shuntAdmittanceToPower(getB(sections, 5), nominalV));
        psseSwitchedShunt.setN7(getN(sections, 6));
        psseSwitchedShunt.setB7(shuntAdmittanceToPower(getB(sections, 6), nominalV));
        psseSwitchedShunt.setN8(getRemainderN(sections, 7));
        psseSwitchedShunt.setB8(shuntAdmittanceToPower(getRemainderB(sections, 7), nominalV));
    }

    private static int getN(List<ShuntCompensatorNonLinearModel.Section> sections, int index) {
        return sections.size() > index ? 1 : 0;
    }

    private static double getB(List<ShuntCompensatorNonLinearModel.Section> sections, int index) {
        return sections.size() > index ? sections.get(index).getB() : 0.0;
    }

    private static int getRemainderN(List<ShuntCompensatorNonLinearModel.Section> sections, int index) {
        return sections.size() > index ? sections.size() - index : 0;
    }

    private static double getRemainderB(List<ShuntCompensatorNonLinearModel.Section> sections, int index) {
        int n = getRemainderN(sections, index);
        if (n <= 0) {
            return 0.0;
        }
        double remainderB = 0.0;
        for (int i = index; i < sections.size(); i++) {
            remainderB += sections.get(i).getB();
        }
        return remainderB / n;
    }

    private static void setDefaultShuntBlocks(PsseSwitchedShunt psseSwitchedShunt) {
        psseSwitchedShunt.setS1(1);
        psseSwitchedShunt.setN1(0);
        psseSwitchedShunt.setB1(0.0);

        psseSwitchedShunt.setS2(1);
        psseSwitchedShunt.setN2(0);
        psseSwitchedShunt.setB2(0.0);

        psseSwitchedShunt.setS3(1);
        psseSwitchedShunt.setN3(0);
        psseSwitchedShunt.setB3(0.0);

        psseSwitchedShunt.setS4(1);
        psseSwitchedShunt.setN4(0);
        psseSwitchedShunt.setB4(0.0);

        psseSwitchedShunt.setS5(1);
        psseSwitchedShunt.setN5(0);
        psseSwitchedShunt.setB5(0.0);

        psseSwitchedShunt.setS6(1);
        psseSwitchedShunt.setN6(0);
        psseSwitchedShunt.setB6(0.0);

        psseSwitchedShunt.setS7(1);
        psseSwitchedShunt.setN7(0);
        psseSwitchedShunt.setB7(0.0);

        psseSwitchedShunt.setS8(1);
        psseSwitchedShunt.setN8(0);
        psseSwitchedShunt.setB8(0.0);
    }

    private final PsseSwitchedShunt psseSwitchedShunt;
    private final PsseVersion version;
    private final NodeBreakerImport nodeBreakerImport;
    private static final Logger LOGGER = LoggerFactory.getLogger(SwitchedShuntCompensatorConverter.class);
}
