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

    public static void createVoltageRegulationBackwardCompatibility(VoltageRegulationAdder<?> adder, double targetV, double targetQ, Boolean voltageRegulatorOn, Terminal terminal) {
        // VOLTAGE case
        if (Boolean.TRUE.equals(voltageRegulatorOn)) {
            adder.withMode(RegulationMode.VOLTAGE)
                .withTargetValue(targetV)
                .withTerminal(terminal)
                .add();
            // REACTIVE Power case
        } else if (Boolean.FALSE.equals(voltageRegulatorOn) && !Double.isNaN(targetQ)) {
            adder.withMode(RegulationMode.REACTIVE_POWER)
                .withTargetValue(targetQ)
                .withTerminal(terminal)
                .add();
        }
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
                .map(Generator::getVoltageRegulation)
                .filter(Objects::nonNull)
                .map(VoltageRegulation::getTargetValue).distinct().toList();
            case SHUNT_COMPENSATOR -> getRegulatingShuntCompensators(network, controlledBus)
                .filter(g -> !g.getId().equals(regulatingElementId))
                .map(ShuntCompensator::getTargetV).distinct().toList();
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

    public static VoltageRegulationData buildVoltageRegulationData(Boolean voltageRegulatorOn, Double voltageSetpoint, Double reactivePowerSetpoint) {
        RegulationMode regulationMode;
        if (voltageRegulatorOn == null) {
            if (!Double.isNaN(voltageSetpoint)) {
                regulationMode = RegulationMode.VOLTAGE;
            } else if (!Double.isNaN(reactivePowerSetpoint)) {
                regulationMode = RegulationMode.REACTIVE_POWER;
            } else {
                regulationMode = RegulationMode.VOLTAGE;
            }
        } else {
            regulationMode = voltageRegulatorOn ? RegulationMode.VOLTAGE : RegulationMode.REACTIVE_POWER;
        }
        double targetValue;
        double targetV = Double.NaN;
        double targetQ = Double.NaN;
        if (regulationMode == RegulationMode.REACTIVE_POWER) {
            targetValue = reactivePowerSetpoint;
            targetV = voltageSetpoint;
        } else {
            targetValue = voltageSetpoint;
            targetQ = reactivePowerSetpoint;
        }
        return new VoltageRegulationData(regulationMode, targetV, targetQ, targetValue);
    }

    public record VoltageRegulationData(RegulationMode regulationMode, double targetV, double targetQ, double targetValue) { }

}
