/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.stateestimation;

import com.powsybl.commons.extensions.Extendable;
import com.powsybl.iidm.network.ThreeSides;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Interface to describe which parts of a network its measurements determine.
 *
 * <p>The vocabulary follows the IIDM observability extensions, so that a provider can publish the
 * same information onto the network without translating it first. The result describes the
 * topology of the variant it was computed on.
 *
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
public interface ObservabilityResult extends Extendable<ObservabilityResult> {

    /** Mirrors {@code ObservabilityArea.ObservabilityStatus}. */
    enum IslandStatus {
        /** The measurements determine the state of every bus in the island. */
        OBSERVABLE,
        /** The measurements leave the state of the island undetermined. */
        NON_OBSERVABLE,
        /** Between an observable island and a non-observable one. */
        BORDER
    }

    /** Whether the analysis ran. */
    enum Status {
        /** The analysis ran and the islands and element observabilities describe the network. */
        COMPLETED,
        /** The analysis could not be carried out, so nothing below is meaningful. */
        FAILED
    }

    /** Whether the analysis ran. */
    Status getStatus();

    /**
     * Implementation-defined text explaining the status, typically why the analysis could not be
     * carried out. Empty when the implementation has nothing to add.
     */
    String getStatusText();

    /** The observable, non-observable and border islands, in no guaranteed order. */
    List<ObservableIsland> getObservableIslands();

    /**
     * The island containing the given bus, or an empty {@code Optional} if there is none.
     *
     * @param busId the identifier of the bus
     * @return the island containing the given bus, or an empty {@code Optional} if there is none
     */
    Optional<ObservableIsland> getIsland(String busId);

    /** Observability of every element the analysis reported on, in no guaranteed order. */
    List<ElementObservability> getElementObservabilities();

    /**
     * Observability of one element, or an empty {@code Optional} if it was not reported on.
     *
     * @param elementId the identifier of the injection or branch
     * @return the observability of that element, or an empty {@code Optional} if it was not reported on
     */
    Optional<ElementObservability> getElementObservability(String elementId);

    /**
     * A connected set of buses sharing one observability status. These are observable islands,
     * which depend on where the measurements are rather than on connectivity, so one connected
     * component of the network can hold several of them.
     */
    interface ObservableIsland {

        /** The number this island carries in {@code ObservabilityArea}, so the two can be matched up. */
        int getIslandNumber();

        /** Whether the measurements determine the state of this island. */
        IslandStatus getStatus();

        /** The buses that belong to this island. */
        Set<String> getBusIds();
    }

    /**
     * Observability of one network element, matching {@code InjectionObservability} for an
     * injection and {@code BranchObservability} for a branch.
     */
    interface ElementObservability {

        /** The IIDM identifier of the injection or branch. */
        String getElementId();

        /**
         * Whether the measurements determine the state of this element. As
         * {@code Observability.isObservable()}, one answer covers every quantity.
         */
        boolean isObservable();

        /** One entry per quantity of this element whose redundancy the analysis established. */
        List<QuantityRedundancy> getRedundancies();
    }

    /**
     * What the analysis determined about one quantity of one element.
     *
     * <p>{@code ObservabilityQuality} also carries a standard deviation. That one describes an
     * estimate rather than the measurement configuration, so it lives on
     * {@link StateEstimationResult.QuantityAccuracy}.
     */
    interface QuantityRedundancy {

        /** The quantity this entry describes. */
        Quantity getQuantity();

        /**
         * The branch side, or an empty {@code Optional} for an injection. Always empty for
         * {@link Quantity#VOLTAGE}, which belongs to a bus rather than to one side of a branch.
         */
        Optional<ThreeSides> getSide();

        /**
         * Whether more than one measurement determines this quantity, or an empty
         * {@code Optional} if the analysis did not establish it.
         */
        Optional<Boolean> isRedundant();
    }
}
