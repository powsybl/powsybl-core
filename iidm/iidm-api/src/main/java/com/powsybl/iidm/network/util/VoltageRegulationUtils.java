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
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.powsybl.iidm.network.regulation.RegulationMode.REACTIVE_POWER;
import static com.powsybl.iidm.network.regulation.RegulationMode.VOLTAGE;
import static com.powsybl.iidm.network.regulation.RegulationMode.VOLTAGE_PER_REACTIVE_POWER;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public final class VoltageRegulationUtils {
    private VoltageRegulationUtils() {
        /* This utility class should not be instantiated */
    }

    public static Set<RegulationMode> getSettableRegulationModes(Class<? extends VoltageRegulationHolder<?>> voltageRegulationHolder, boolean isTerminalSet, boolean regulating) {
        if (!regulating) {
            // When the regulation is not set, we ignore the terminal and return all the settable modes
            return Stream.concat(
                    getRemoteSettableRegulationModes(voltageRegulationHolder).stream(),
                    getLocalSettableRegulationModes(voltageRegulationHolder).stream())
                    .collect(Collectors.toSet());
        }
        return isTerminalSet ? getRemoteSettableRegulationModes(voltageRegulationHolder) : getLocalSettableRegulationModes(voltageRegulationHolder);
    }

    private static Set<RegulationMode> getRemoteSettableRegulationModes(Class<? extends VoltageRegulationHolder<?>> voltageRegulationHolder) {
        return switch (voltageRegulationHolder) {
            case Class<?> c when c == Battery.class -> Set.of(VOLTAGE, REACTIVE_POWER);
            case Class<?> c when c == Generator.class -> Set.of(VOLTAGE, REACTIVE_POWER); // REACTIVE_POWER_PER_ACTIVE_POWER not yet supported
            case Class<?> c when c == RatioTapChanger.class -> Set.of(VOLTAGE, REACTIVE_POWER);
            case Class<?> c when c == ShuntCompensator.class -> Set.of(VOLTAGE);
            case Class<?> c when c == StaticVarCompensator.class -> Set.of(VOLTAGE, REACTIVE_POWER, VOLTAGE_PER_REACTIVE_POWER);
            case Class<?> c when c == VscConverterStation.class -> Set.of(VOLTAGE, REACTIVE_POWER);
            case Class<?> c when c == VoltageSourceConverter.class -> Set.of(VOLTAGE, REACTIVE_POWER);
            default -> throwError(voltageRegulationHolder);
        };
    }

    private static Set<RegulationMode> getLocalSettableRegulationModes(Class<? extends VoltageRegulationHolder<?>> voltageRegulationHolder) {
        return switch (voltageRegulationHolder) {
            case Class<?> c when c == Battery.class -> Set.of(VOLTAGE);
            case Class<?> c when c == Generator.class -> Set.of(VOLTAGE); // REACTIVE_POWER_PER_ACTIVE_POWER not yet supported
            case Class<?> c when c == RatioTapChanger.class -> Set.of();
            case Class<?> c when c == ShuntCompensator.class -> Set.of(VOLTAGE);
            case Class<?> c when c == StaticVarCompensator.class -> Set.of(VOLTAGE, REACTIVE_POWER, VOLTAGE_PER_REACTIVE_POWER);
            case Class<?> c when c == VscConverterStation.class -> Set.of(VOLTAGE);
            case Class<?> c when c == VoltageSourceConverter.class -> Set.of(VOLTAGE);
            default -> throwError(voltageRegulationHolder);
        };
    }

    private static Set<RegulationMode> throwError(Class<? extends VoltageRegulationHolder<?>> voltageRegulationHolder) {
        throw new IllegalArgumentException(voltageRegulationHolder.getSimpleName() + " class cannot be used with VoltageRegulation");
    }

    private static <T extends VoltageRegulationHolderAdder<T>> void createVoltageRegulationBackwardCompatibility(VoltageRegulationHolderAdder<T> adder,
                                                                                                          boolean withLocalTargetValue,
                                                                                                          double targetV,
                                                                                                          double localTargetV,
                                                                                                          double targetQ,
                                                                                                          Boolean voltageRegulatorOn,
                                                                                                          Terminal terminal) {
        // VOLTAGE case
        if (Boolean.TRUE.equals(voltageRegulatorOn)) {
            VoltageRegulationAdder<T> vrAdder = adder.newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE);
            if (terminal != null) {
                vrAdder.withTargetValue(targetV)
                    .withTerminal(terminal);
                if (withLocalTargetValue) {
                    adder.setLocalTargetV(localTargetV);
                }
            } else {
                adder.setLocalTargetV(targetV);
            }
            vrAdder.add();
            adder.setLocalTargetQ(targetQ);
            // REACTIVE Power case
        } else if (Boolean.FALSE.equals(voltageRegulatorOn) && !Double.isNaN(targetQ) && terminal != null) {
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
                                                                                                                double targetV,
                                                                                                                double localTargetV,
                                                                                                                double targetQ,
                                                                                                                Boolean voltageRegulatorOn,
                                                                                                                Terminal terminal) {
        createVoltageRegulationBackwardCompatibility(adder, true, targetV, localTargetV, targetQ, voltageRegulatorOn, terminal);
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
        return getRegulating(network.getGeneratorStream(), controlledBus);
    }

    /**
     * Get the stream of all shunt compensators of the network controlling voltage at controlled bus.
     */
    public static Stream<ShuntCompensator> getRegulatingShuntCompensators(Network network, Bus controlledBus) {
        return getRegulating(network.getShuntCompensatorStream(), controlledBus);
    }

    private static @NonNull <T extends VoltageRegulationHolder<?>> Stream<T> getRegulating(Stream<T> voltageRegulationHolderStream, Bus controlledBus) {
        if (controlledBus != null) {
            return voltageRegulationHolderStream
                    .filter(holder -> holder.isRegulatingWithMode(RegulationMode.VOLTAGE))
                    .filter(holder -> holder.getRegulatingTerminal().getBusView().getBus() != null)
                    .filter(holder -> controlledBus.equals(holder.getRegulatingTerminal().getBusView().getBus()));
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
        List<Double> targets = List.of();
        Stream<? extends VoltageRegulationHolder<?>> holderStream = switch (identifiableType) {
            case GENERATOR -> network.getGeneratorStream();
            case SHUNT_COMPENSATOR -> network.getShuntCompensatorStream();
            default -> null;
        };
        if (holderStream != null) {
            targets = getRegulating(holderStream, controlledBus)
                    .filter(h -> !((Identifiable<?>) h).getId().equals(regulatingElementId))
                    .map(VoltageRegulationHolder::getRegulatingTargetV)
                    .distinct().toList();
        }

        if (targets.size() != 1) {
            // it means that the network cannot give valuable information about targetV, this field has to be given in
            // the network modification.
            return OptionalDouble.empty();
        } else { // targets.size() == 1
            return OptionalDouble.of(targets.getFirst());
        }
    }

    public static boolean haveSameConnectableBus(Terminal regulatingTerminal, Terminal terminal) {
        return regulatingTerminal != null && regulatingTerminal.getVoltageLevel() != null
            && terminal != null && terminal.getVoltageLevel() != null
            && Objects.equals(regulatingTerminal.getBusBreakerView().getConnectableBus(), terminal.getBusBreakerView().getConnectableBus());
    }

    public static boolean isRegulating(VoltageRegulation voltageRegulation, Terminal terminal) {
        boolean isRemoteReactivePowerRegulating = voltageRegulation != null
            && (RegulationMode.REACTIVE_POWER.equals(voltageRegulation.getMode())
            || !haveSameConnectableBus(voltageRegulation.getTerminal(), terminal));

        return voltageRegulation != null
            && voltageRegulation.isRegulating()
            || isRemoteReactivePowerRegulating;
    }

    public static void buildVoltageRegulation(VoltageRegulationHolder<?> holder, boolean isLocalTerminal, double targetV, Terminal regulatingTerminal, boolean isRegulatingOn) {
        if (isLocalTerminal) {
            holder.setLocalTargetV(targetV);
            if (isRegulatingOn) {
                holder.newVoltageRegulation()
                    .withMode(RegulationMode.VOLTAGE)
                    .build();
            }
        } else {
            holder.newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withTerminal(regulatingTerminal)
                .withTargetValue(targetV)
                .withRegulating(isRegulatingOn)
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
        return new VoltageRegulationData(regulationMode, voltageSetpoint, reactivePowerSetpoint, Double.NaN);
    }

    public record VoltageRegulationData(RegulationMode regulationMode, double targetV, double targetQ, double targetValue) { }

}
