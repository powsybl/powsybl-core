/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.util;

import com.powsybl.iidm.network.*;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Stream;

public final class VoltageRegulationUtils {

    private VoltageRegulationUtils() {
    }

    /**
     * Get the stream of all generators of the network controlling voltage at controlled bus.
     */
    public static Stream<Generator> getRegulatingGenerators(Network network, Bus controlledBus) {
        if (controlledBus != null) {
            return network.getGeneratorStream().filter(Generator::isVoltageRegulatorOn)
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
            return network.getShuntCompensatorStream().filter(ShuntCompensator::isVoltageRegulatorOn)
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
                    .map(Generator::getTargetV).distinct().toList();
            case SHUNT_COMPENSATOR -> getRegulatingShuntCompensators(network, controlledBus)
                    .filter(g -> !g.getId().equals(regulatingElementId))
                    .map(ShuntCompensator::getTargetV).distinct().toList();
            default -> new ArrayList<>();
        };
        if (targets.isEmpty() || targets.size() > 1) {
            // it means that the network cannot give valuable information about targetV, this field has to be given in
            // the network modification.
            return OptionalDouble.empty();
        } else { // targets.size() == 1
            return OptionalDouble.of(targets.get(0));
        }
    }

}
