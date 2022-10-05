/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.*;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Utility class for creating equipment from existing network components.
 *
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public final class CopyUtil {

    /**
     * Copy ID, name and fictitious status from an existing identifiable to an identifiable adder.
     * @return The updated adder.
     */
    public static <I extends Identifiable<I>, A extends IdentifiableAdder<A>> A copyIdNameFictitious(I identifiable, A adder) {
        adder.setId(identifiable.getId()).setFictitious(identifiable.isFictitious());
        identifiable.getOptionalName().ifPresent(adder::setName);
        return adder;
    }

    /**
     * Copy ID, name, fictitious status, ends' voltage levels' IDs and nodes or buses from an existing branch to a branch adder.
     * @return The updated adder.
     */
    public static <B extends Branch<B>, A extends BranchAdder<A>> A copyIdNameFictitiousConnectivity(B branch, A adder) {
        copyIdNameFictitious(branch, adder);
        copyConnectivity(branch.getTerminal1().getVoltageLevel(), adder::setVoltageLevel1, () -> branch
                        .getTerminal1().getNodeBreakerView().getNode(), () -> branch
                        .getTerminal1().getBusBreakerView().getConnectableBus().getId(), () -> branch
                        .getTerminal1().getBusBreakerView().getBus(),
                adder::setNode1, adder::setConnectableBus1, adder::setBus1);
        copyConnectivity(branch.getTerminal2().getVoltageLevel(), adder::setVoltageLevel2, () -> branch
                        .getTerminal2().getNodeBreakerView().getNode(), () -> branch
                        .getTerminal2().getBusBreakerView().getConnectableBus().getId(), () -> branch
                        .getTerminal2().getBusBreakerView().getBus(),
                adder::setNode2, adder::setConnectableBus2, adder::setBus2);
        return adder;
    }

    /**
     * Copy voltage level and node or bus from an existing network component to a new one.
     *
     * @param voltageLevelSetter Set the voltage level to the new network component.
     * @param nodeGetter Get the node from the existing network component.
     * @param connectableBusGetter Get the connectable bus ID from the existing network component.
     * @param busGetter Get the bus from the existing network component.
     * @param nodeSetter Set the node to the new network component.
     * @param connectableBusSetter Set the connectable bus ID to the new network component.
     * @param busSetter Set the bus ID to the new network component.
     */
    public static void copyConnectivity(VoltageLevel voltageLevel, Consumer<String> voltageLevelSetter,
                                        IntSupplier nodeGetter, Supplier<String> connectableBusGetter, Supplier<Bus> busGetter,
                                        IntConsumer nodeSetter, Consumer<String> connectableBusSetter, Consumer<String> busSetter) {
        voltageLevelSetter.accept(voltageLevel.getId());
        if (voltageLevel.getTopologyKind() == TopologyKind.NODE_BREAKER) {
            nodeSetter.accept(nodeGetter.getAsInt());
        } else {
            connectableBusSetter.accept(connectableBusGetter.get());
            Optional.ofNullable(busGetter.get()).ifPresent(b -> busSetter.accept(b.getId()));
        }
    }

    private CopyUtil() {
    }
}
