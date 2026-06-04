/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.*;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.stream.Stream;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public final class VoltageRegulationUtils {
    private VoltageRegulationUtils() {
        /* This utility class should not be instantiated */
    }

    private static <T extends VoltageRegulationHolderAdder<T>> void createVoltageRegulationBackwardCompatibility(VoltageRegulationHolderAdder<T> adder,
                                                                                                          boolean withTargetValue,
                                                                                                          double targetValue,
                                                                                                          double localTargetV,
                                                                                                          double targetQ,
                                                                                                          Boolean voltageRegulatorOn,
                                                                                                          Terminal terminal) {
        // VOLTAGE case
        if (Boolean.TRUE.equals(voltageRegulatorOn)) {
            VoltageRegulationAdder<T> vrAdder = adder.newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE);
            if (terminal != null) {
                vrAdder.withTargetValue(targetValue)
                    .withTerminal(terminal);
            }
            if (withTargetValue || terminal == null) {
                adder.setLocalTargetV(localTargetV);
            }
            vrAdder.add();
            adder.setLocalTargetQ(targetQ);
            // REACTIVE Power case
        } else if (Boolean.FALSE.equals(voltageRegulatorOn)
            && !Double.isNaN(targetQ)
            && terminal != null) {
            adder.newVoltageRegulation()
                .withMode(RegulationMode.REACTIVE_POWER)
                .withTargetValue(targetQ)
                .withTerminal(terminal)
                .add();
        } else {
            adder.setLocalTargetV(localTargetV);
            adder.setLocalTargetQ(targetQ);
        }

    }

    public static <T extends VoltageRegulationHolderAdder<T>> void createVoltageRegulationBackwardCompatibility(VoltageRegulationHolderAdder<T> adder,
                                                                                                                double targetValue,
                                                                                                                double localTargetV,
                                                                                                                double targetQ,
                                                                                                                Boolean voltageRegulatorOn,
                                                                                                                Terminal terminal) {
        createVoltageRegulationBackwardCompatibility(adder, true, targetValue, localTargetV, targetQ, voltageRegulatorOn, terminal);
    }

    public static <T extends VoltageRegulationHolderAdder<T>> void createVoltageRegulationBackwardCompatibility(VoltageRegulationHolderAdder<T> adder,
                                                                                                                double targetV,
                                                                                                                double targetQ,
                                                                                                                Boolean voltageRegulatorOn,
                                                                                                                Terminal terminal) {
        createVoltageRegulationBackwardCompatibility(adder, false, targetV, targetV, targetQ, voltageRegulatorOn, terminal);
    }

    /**
     * Get the stream of all generators of the network controlling voltage at controlled bus.
     */
    public static Stream<Generator> getRegulatingGenerators(Network network, Bus controlledBus) {
        if (controlledBus != null) {
            return network.getGeneratorStream()
                .filter(g -> g.isRegulatingWithMode(RegulationMode.VOLTAGE))
                .filter(g -> g.getRegulatingTerminal().getBusView().getBus() != null)
                .filter(g -> controlledBus.equals(g.getRegulatingTerminal().getBusView().getBus()));
        } else {
            return Stream.empty();
        }
    }

    /**
     * Get the stream of all shunt compensators of the network controlling voltage at controlled bus.
     */
    public static Stream<ShuntCompensator> getRegulatingShuntCompensators(Network network, Bus controlledBus) {
        if (controlledBus != null) {
            return network.getShuntCompensatorStream()
                    .filter(shuntCompensator -> shuntCompensator.isRegulatingWithMode(RegulationMode.VOLTAGE))
                    .filter(sh -> sh.getRegulatingTerminal().getBusView().getBus() != null)
                    .filter(sh -> controlledBus.equals(sh.getRegulatingTerminal().getBusView().getBus()));
        } else {
            return Stream.empty();
        }
    }

    /**
     * A method to find an acceptable target voltage for a regulating element to be connected. Only generators and shunt
     * compensators are yet supported. As a first version, we don't consider heterogeneous voltage controls.
     *
     * @param controlledBus       the controlled bus of the regulating terminal of the regulating element
     * @param regulatingElementId the id of the regulating element
     * @return the targetV, or {@link OptionalDouble#empty()} if no acceptable targetV has been found
     */
    public static OptionalDouble getTargetVForRegulatingElement(Network network, Bus controlledBus, String regulatingElementId,
                                                                IdentifiableType identifiableType) {
        List<Double> targets = switch (identifiableType) {
            case GENERATOR -> getRegulatingGenerators(network, controlledBus)
                .filter(g -> !g.getId().equals(regulatingElementId))
                .map(Generator::getRegulatingTargetV)
                .distinct().toList();
            case SHUNT_COMPENSATOR -> getRegulatingShuntCompensators(network, controlledBus)
                .filter(g -> !g.getId().equals(regulatingElementId))
                .map(ShuntCompensator::getRegulatingTargetV).distinct().toList();
            default -> new ArrayList<>();
        };
        if (targets.size() != 1) {
            // it means that the network cannot give valuable information about targetV, this field has to be given in
            // the network modification.
            return OptionalDouble.empty();
        } else { // targets.size() == 1
            return OptionalDouble.of(targets.getFirst());
        }
    }

    public static <A extends VoltageRegulationHolder & Identifiable<?>> boolean logMissingVoltageRegulation(A holder, Logger logger, String type, String message) {
        if (holder != null && holder.getVoltageRegulation() == null) {
            logger.warn("Missing VoltageRegulation in {} '{}': {}", type, holder.getId(), message);
            return true;
        }
        return false;
    }

    public static boolean haveSameConnectableBus(Terminal regulatingTerminal, Terminal terminal) {
        return regulatingTerminal == null || regulatingTerminal.getVoltageLevel() == null
            || terminal == null || terminal.getVoltageLevel() == null
            || Objects.equals(regulatingTerminal.getBusBreakerView().getConnectableBus(), terminal.getBusBreakerView().getConnectableBus());
    }

    public static boolean isRegulating(VoltageRegulation voltageRegulation, Terminal terminal) {
        boolean isRemoteReactivePowerRegulating = voltageRegulation != null
            && (RegulationMode.REACTIVE_POWER.equals(voltageRegulation.getMode())
            || !haveSameConnectableBus(voltageRegulation.getTerminal(), terminal));

        return voltageRegulation != null
            && voltageRegulation.isRegulating()
            || isRemoteReactivePowerRegulating;
    }

    public static void buildVoltageRegulation(VoltageRegulationHolder holder, boolean isLocalTerminal, double targetV, Terminal regulatingTerminal, boolean isRegulatingOn) {
        if (isLocalTerminal) {
            holder.setLocalTargetV(targetV);
        }
        if (!isLocalTerminal) {
            holder.newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withTargetValue(targetV)
                .withTerminal(regulatingTerminal)
                .withTargetValue(targetV)
                .withRegulating(isRegulatingOn)
                .build();
        } else if (isRegulatingOn) {
            holder.newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .build();
        }
    }

    public static VoltageRegulationData buildVoltageRegulationData(Boolean voltageRegulatorOn, Double voltageSetpoint, Double reactivePowerSetpoint) {
        RegulationMode regulationMode;
        if (voltageRegulatorOn == null) {
            if (!Double.isNaN(voltageSetpoint)) {
                regulationMode = RegulationMode.VOLTAGE;
            } else if (!Double.isNaN(reactivePowerSetpoint)) {
                regulationMode = null;
            } else {
                regulationMode = RegulationMode.VOLTAGE;
            }
        } else {
            regulationMode = voltageRegulatorOn ? RegulationMode.VOLTAGE : null;
        }
        double targetValue = Double.NaN;
        return new VoltageRegulationData(regulationMode, voltageSetpoint, reactivePowerSetpoint, targetValue);
    }

    public record VoltageRegulationData(RegulationMode regulationMode, double targetV, double targetQ, double targetValue) { }

}
