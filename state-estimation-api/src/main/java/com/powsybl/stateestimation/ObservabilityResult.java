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

    /** The quantities modelled by {@code InjectionObservability} and {@code BranchObservability}. */
    enum Quantity {
        ACTIVE_POWER,
        REACTIVE_POWER,
        VOLTAGE
    }

    /** Mirrors {@code ObservabilityArea.ObservabilityStatus}. */
    enum AreaStatus {
        OBSERVABLE,
        NON_OBSERVABLE,
        BORDER // between an observable area and a non-observable one
    }

    List<Area> getAreas();

    /** The area containing the given bus, or an empty {@code Optional} if there is none. */
    Optional<Area> getArea(String busId);

    List<ElementObservability> getElementObservabilities();

    Optional<ElementObservability> getElementObservability(String elementId);

    /** A connected set of buses sharing one observability status. */
    interface Area {

        /** The area number, as {@code ObservabilityArea.AreaCharacteristics} numbers it. */
        int getAreaNumber();

        AreaStatus getStatus();

        Set<String> getBusIds();
    }

    /**
     * Observability of one network element, matching {@code InjectionObservability} for an
     * injection and {@code BranchObservability} for a branch.
     */
    interface ElementObservability {

        String getElementId();

        /** As {@code Observability.isObservable()}: one answer for every quantity. */
        boolean isObservable();

        List<Quality> getQualities();
    }

    /** What was determined about one quantity of one element, matching {@code ObservabilityQuality}. */
    interface Quality {

        Quantity getQuantity();

        /** The branch side, or an empty {@code Optional} for an injection. */
        Optional<ThreeSides> getSide();

        /** The standard deviation of the estimated quantity, in MW, MVar or kV. */
        double getStandardDeviation();

        /**
         * Whether more than one measurement determines this quantity, or an empty
         * {@code Optional} if the estimation did not establish it.
         */
        Optional<Boolean> isRedundant();
    }
}
