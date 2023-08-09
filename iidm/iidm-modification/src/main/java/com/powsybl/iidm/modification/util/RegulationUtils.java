/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.util;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Stream;

public final class RegulationUtils {
    private RegulationUtils() {
    }

    /**
     * Get the stream of all generators of the network regulating the bus.
     */
    public static Stream<Generator> getRegulatingGenerators(Network network, Bus regulatingBus) {
        return network.getGeneratorStream().filter(Generator::isVoltageRegulatorOn)
            .filter(g -> regulatingBus.equals(g.getRegulatingTerminal().getBusView().getBus()));
    }

    /**
     * Get the stream of all shunt compensators of the network regulating the bus.
     */
    public static Stream<ShuntCompensator> getRegulatingShuntCompensators(Network network, Bus regulatingBus) {
        return network.getShuntCompensatorStream().filter(ShuntCompensator::isVoltageRegulatorOn)
            .filter(shunt -> regulatingBus.equals(shunt.getRegulatingTerminal().getBusView().getBus()));
    }

    /**
     * Finds the correct target voltage for a regulating element.
     *
     * @param regulatingBus       the regulating bus on which the regulating element will be connected.
     * @param shuntToIgnoreId     allow to ignore one shunt in the search, useful when we have reconnected a shunt and want to set its target v
     * @param generatorToIgnoreId same as param shuntToIgnoreId, but for a generator
     * @return the targetV, or {@link OptionalDouble#empty()} if there are no regulating shunts, generators, and the regulating bus does not have a valid voltage
     */
    private static OptionalDouble getTargetVForRegulatingElement(Network network, Bus regulatingBus,
                                                                 String shuntToIgnoreId,
                                                                 String generatorToIgnoreId) {
        // first we search for a shunt regulating the bus (appart from the one given in parameters)
        Optional<Double> optDouble =
            getRegulatingShuntCompensators(network, regulatingBus)
                .filter(sc -> !sc.getId().equals(shuntToIgnoreId))
                .map(ShuntCompensator::getTargetV).findFirst()
                // if none were found, we search for a generator regulating the bus (appart from the one given in parameters)
                .or(() -> getRegulatingGenerators(network, regulatingBus)
                    .filter(generator -> !generator.getId().equals(generatorToIgnoreId))
                    .map(Generator::getTargetV).findFirst())
                // if none were found, we use the network voltage, if it is not NaN else we return an empty optional.
                .or(() -> Double.isNaN(regulatingBus.getV()) ? Optional.empty() : Optional.of(regulatingBus.getV()));
        return optDouble.isPresent() ? OptionalDouble.of(optDouble.get()) : OptionalDouble.empty();
    }

    /**
     * Tries to find the regulating voltage for a regulating shunt on the given regulating bus.
     *
     * @return the targetV, or {@link OptionalDouble#empty()} if no valid values are found (no regulating elements and NaN network voltage on the bus)
     */
    public static OptionalDouble getTargetVForRegulatingShunt(Network network, Bus regulatingBus,
                                                              String shuntToIgnoreId) {
        return getTargetVForRegulatingElement(network, regulatingBus, shuntToIgnoreId, null);
    }

    /**
     * Tries to find the regulating voltage for a regulating generator on the given regulating bus.
     *
     * @return the targetV, or {@link OptionalDouble#empty()} if no valid values are found (no regulating elements and NaN network voltage on the bus)
     */
    public static OptionalDouble getTargetVForRegulatingGenerator(Network network, Bus regulatingBus,
                                                                  String generatorToIgnoreId) {
        return getTargetVForRegulatingElement(network, regulatingBus, null, generatorToIgnoreId);
    }

    /**
     * Tries to find the regulating voltage for a regulating shunt.
     *
     * @return the targetV, or {@link OptionalDouble#empty()} if no valid values are found (no regulating elements and NaN network voltage on the bus)
     */
    public static OptionalDouble getTargetVForRegulatingShunt(ShuntCompensator shuntCompensator) {
        Bus regulatingBus = shuntCompensator.getRegulatingTerminal().getBusView().getBus();
        if (regulatingBus != null) {
            return RegulationUtils.getTargetVForRegulatingShunt(shuntCompensator.getNetwork(), regulatingBus,
                shuntCompensator.getId());
        }
        return OptionalDouble.empty();
    }

    /**
     * Tries to find the regulating voltage for a regulating generator.
     *
     * @return the targetV, or {@link OptionalDouble#empty()} if no valid values are found (no regulating elements and NaN network voltage on the bus)
     */
    public static OptionalDouble getTargetVForRegulatingGenerator(Generator generator) {
        Bus regulatingBus = generator.getRegulatingTerminal().getBusView().getBus();
        if (regulatingBus != null) {
            return RegulationUtils.getTargetVForRegulatingGenerator(generator.getNetwork(), regulatingBus,
                generator.getId());
        }
        return OptionalDouble.empty();
    }
}
