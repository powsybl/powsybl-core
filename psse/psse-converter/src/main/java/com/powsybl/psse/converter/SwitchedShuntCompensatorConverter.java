/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.pf.PssePowerFlowModel;
import com.powsybl.psse.model.pf.PsseSwitchedShunt;
import static com.powsybl.psse.model.PsseVersion.Major.V35;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class SwitchedShuntCompensatorConverter extends AbstractConverter {

    SwitchedShuntCompensatorConverter(PsseSwitchedShunt psseSwitchedShunt, ContainersMapping containerMapping, Network network, PsseVersion version) {
        super(containerMapping, network);
        this.psseSwitchedShunt = Objects.requireNonNull(psseSwitchedShunt);
        this.version = Objects.requireNonNull(version);
    }

    void create() {
        List<ShuntBlock> shuntBlocks = defineShuntBlocks(psseSwitchedShunt, version);
        if (shuntBlocks.isEmpty()) {
            return;
        }

        String busId = getBusId(psseSwitchedShunt.getI());
        String id = defineShuntId(psseSwitchedShunt, version);
        VoltageLevel voltageLevel = getNetwork().getVoltageLevel(getContainersMapping().getVoltageLevelId(psseSwitchedShunt.getI()));

        ShuntCompensatorAdder adder = voltageLevel.newShuntCompensator()
            .setId(getShuntId(busId, id))
            .setConnectableBus(busId)
            .setSectionCount(defineSectionCount(psseSwitchedShunt.getBinit(), shuntBlocks));
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

        adder.setBus(psseSwitchedShunt.getStat() == 1 ? busId : null);
        adder.add();
    }

    void addControl() {
        String busId = getBusId(psseSwitchedShunt.getI());
        String id = defineShuntId(psseSwitchedShunt, version);
        ShuntCompensator shunt = getNetwork().getShuntCompensator(getShuntId(busId, id));

        // Add control only if shunt has been created
        if (shunt == null) {
            return;
        }

        Terminal regulatingTerminal = defineRegulatingTerminal(psseSwitchedShunt, getNetwork(), version);
        // Discard control if the switchedShunt is controlling an isolated bus
        if (regulatingTerminal == null) {
            return;
        }
        boolean psseVoltageRegulatorOn = defineVoltageRegulatorOn(psseSwitchedShunt);
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

    private static boolean defineVoltageRegulatorOn(PsseSwitchedShunt psseSwitchedShunt) {
        return psseSwitchedShunt.getModsw() != 0;
    }

    // Nreg (version 35) is not yet considered
    private static Terminal defineRegulatingTerminal(PsseSwitchedShunt psseSwitchedShunt, Network network, PsseVersion version) {
        String defaultRegulatingBusId = getBusId(psseSwitchedShunt.getI());
        Terminal regulatingTerminal = null;
        if (switchedShuntRegulatingBus(psseSwitchedShunt, version) == 0) {
            Bus bus = network.getBusBreakerView().getBus(defaultRegulatingBusId);
            regulatingTerminal = bus.getConnectedTerminalStream().findFirst().orElse(null);
        } else {
            String regulatingBusId = getBusId(switchedShuntRegulatingBus(psseSwitchedShunt, version));
            Bus bus = network.getBusBreakerView().getBus(regulatingBusId);
            if (bus != null) {
                regulatingTerminal = bus.getConnectedTerminalStream().findFirst().orElse(null);
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

            LOGGER.warn("Switched combination not exactly supported ({})",
                getShuntId(getBusId(psseSwitchedShunt.getI()), defineShuntId(psseSwitchedShunt, version)));
        }

        double bAdd = 0.0;
        List<ShuntBlock> shuntBlocks = new ArrayList<>();
        if (!psseReactorBlocks.isEmpty()) {
            for (int i = 0; i < psseReactorBlocks.size(); i++) {
                for (int j = 0; j < psseReactorBlocks.get(i).getN(); j++) {
                    bAdd = bAdd + psseReactorBlocks.get(i).getB();
                    shuntBlocks.add(new ShuntBlock(1, 1, bAdd));
                }
            }
        }

        if (psseSwitchedShunt.getAdjm() == 1) {
            bAdd = 0.0;
        }

        if (!psseCapacitorBlocks.isEmpty()) {
            for (int i = 0; i < psseCapacitorBlocks.size(); i++) {
                for (int j = 0; j < psseCapacitorBlocks.get(i).getN(); j++) {
                    bAdd = bAdd + psseCapacitorBlocks.get(i).getB();
                    shuntBlocks.add(new ShuntBlock(1, 1, bAdd));
                }
            }
        }

        // Add the zero block, shunt disconnected
        shuntBlocks.add(new ShuntBlock(1, 1, 0.0));

        shuntBlocks.sort(Comparator.comparing(ShuntBlock::getB));

        return shuntBlocks;
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

    private static String getShuntId(String busId, String id) {
        return busId + "-SwSH" + id;
    }

    // At the moment we do not consider new switchedShunts
    static void updateSwitchedShunts(Network network, PssePowerFlowModel psseModel, PssePowerFlowModel updatePsseModel) {
        PsseVersion version = PsseVersion.fromRevision(updatePsseModel.getCaseIdentification().getRev());
        psseModel.getSwitchedShunts().forEach(psseSwitchedShunt -> {
            updatePsseModel.addSwitchedShunts(Collections.singletonList(psseSwitchedShunt));
            PsseSwitchedShunt updatePsseSwitchedShunt = updatePsseModel.getSwitchedShunts().get(updatePsseModel.getSwitchedShunts().size() - 1);

            String switchedShuntId = getShuntId(getBusId(updatePsseSwitchedShunt.getI()), defineShuntId(updatePsseSwitchedShunt, version));
            ShuntCompensator switchedShunt = network.getShuntCompensator(switchedShuntId);
            if (switchedShunt == null) {
                updatePsseSwitchedShunt.setStat(0);
            } else {
                updatePsseSwitchedShunt.setStat(getStatus(switchedShunt));
                updatePsseSwitchedShunt.setBinit(getQ(switchedShunt));
            }
        });
    }

    private static int getStatus(ShuntCompensator switchedShunt) {
        if (switchedShunt.getTerminal().isConnected()) {
            return 1;
        } else {
            return 0;
        }
    }

    private static double getQ(ShuntCompensator switchedShunt) {
        return shuntAdmittanceToPower(switchedShunt.getB(switchedShunt.getSectionCount()),
            switchedShunt.getTerminal().getVoltageLevel().getNominalV());
    }

    private final PsseSwitchedShunt psseSwitchedShunt;
    private final PsseVersion version;

    private static final Logger LOGGER = LoggerFactory.getLogger(SwitchedShuntCompensatorConverter.class);
}
