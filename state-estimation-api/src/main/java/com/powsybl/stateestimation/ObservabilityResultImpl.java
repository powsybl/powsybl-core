/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.stateestimation;

import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.iidm.network.ThreeSides;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Immutable {@link ObservabilityResult}. Every collection passed in is copied.
 *
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
public class ObservabilityResultImpl extends AbstractExtendable<ObservabilityResult>
        implements ObservabilityResult {

    private final Status status;
    private final String statusText;
    private final List<ObservableIsland> islands;
    private final List<ElementObservability> elementObservabilities;
    private final Map<String, ObservableIsland> islandsByBusId;
    private final Map<String, ElementObservability> observabilitiesByElementId;

    /**
     * {@code statusText} may be null and is read as empty; nothing else may.
     *
     * @param status whether the analysis ran
     * @param statusText text explaining the status, or null for none
     * @param islands the observable, non-observable and border islands
     * @param elementObservabilities the observability of each element reported on
     */
    public ObservabilityResultImpl(Status status, String statusText, List<ObservableIsland> islands,
                                   List<ElementObservability> elementObservabilities) {
        this.status = Objects.requireNonNull(status);
        this.statusText = Objects.requireNonNullElse(statusText, "");
        this.islands = List.copyOf(Objects.requireNonNull(islands));
        this.elementObservabilities = List.copyOf(Objects.requireNonNull(elementObservabilities));
        this.islandsByBusId = new HashMap<>();
        for (ObservableIsland island : this.islands) {
            island.getBusIds().forEach(busId -> islandsByBusId.putIfAbsent(busId, island));
        }
        this.observabilitiesByElementId = new HashMap<>();
        for (ElementObservability observability : this.elementObservabilities) {
            observabilitiesByElementId.putIfAbsent(observability.getElementId(), observability);
        }
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public String getStatusText() {
        return statusText;
    }

    @Override
    public List<ObservableIsland> getObservableIslands() {
        return islands;
    }

    @Override
    public Optional<ObservableIsland> getIsland(String busId) {
        Objects.requireNonNull(busId);
        return Optional.ofNullable(islandsByBusId.get(busId));
    }

    @Override
    public List<ElementObservability> getElementObservabilities() {
        return elementObservabilities;
    }

    @Override
    public Optional<ElementObservability> getElementObservability(String elementId) {
        Objects.requireNonNull(elementId);
        return Optional.ofNullable(observabilitiesByElementId.get(elementId));
    }

    /** Immutable {@link ObservableIsland}. */
    public static class ObservableIslandImpl implements ObservableIsland {

        private final int islandNumber;
        private final IslandStatus status;
        private final Set<String> busIds;

        /**
         * Neither {@code status} nor {@code busIds} may be null.
         *
         * @param islandNumber the island number
         * @param status the outcome being recorded
         * @param busIds the buses that belong to the island
         */
        public ObservableIslandImpl(int islandNumber, IslandStatus status, Set<String> busIds) {
            this.islandNumber = islandNumber;
            this.status = Objects.requireNonNull(status);
            this.busIds = Set.copyOf(Objects.requireNonNull(busIds));
        }

        @Override
        public int getIslandNumber() {
            return islandNumber;
        }

        @Override
        public IslandStatus getStatus() {
            return status;
        }

        @Override
        public Set<String> getBusIds() {
            return busIds;
        }
    }

    /** Immutable {@link ElementObservability}. */
    public static class ElementObservabilityImpl implements ElementObservability {

        private final String elementId;
        private final boolean observable;
        private final List<QuantityRedundancy> redundancies;

        /**
         * Neither {@code elementId} nor {@code redundancies} may be null.
         *
         * @param elementId the identifier of the injection or branch
         * @param observable whether the measurements determine the state of the element
         * @param redundancies one entry per quantity determined for the element
         */
        public ElementObservabilityImpl(String elementId, boolean observable, List<QuantityRedundancy> redundancies) {
            this.elementId = Objects.requireNonNull(elementId);
            this.observable = observable;
            this.redundancies = List.copyOf(Objects.requireNonNull(redundancies));
        }

        @Override
        public String getElementId() {
            return elementId;
        }

        @Override
        public boolean isObservable() {
            return observable;
        }

        @Override
        public List<QuantityRedundancy> getRedundancies() {
            return redundancies;
        }
    }

    /** Immutable {@link QuantityRedundancy}. */
    public static class QuantityRedundancyImpl implements QuantityRedundancy {

        private final Quantity quantity;
        private final ThreeSides side;
        private final Boolean redundant;

        /**
         * {@code side} is null for an injection, and {@code redundant} is null when the analysis did
         * not establish whether the quantity is redundantly measured.
         *
         * @param quantity the measured quantity
         * @param side the branch side, or null for an injection
         * @param redundant whether more than one measurement determines the quantity, or null if that was not established
         * @throws IllegalArgumentException if a voltage is given a branch side
         */
        public QuantityRedundancyImpl(Quantity quantity, ThreeSides side, Boolean redundant) {
            this.quantity = Objects.requireNonNull(quantity);
            this.side = checkSide(quantity, side);
            this.redundant = redundant;
        }

        @Override
        public Quantity getQuantity() {
            return quantity;
        }

        @Override
        public Optional<ThreeSides> getSide() {
            return Optional.ofNullable(side);
        }

        @Override
        public Optional<Boolean> isRedundant() {
            return Optional.ofNullable(redundant);
        }

        /** Voltage belongs to a bus, and {@code BranchObservability} carries no voltage quality. */
        private static ThreeSides checkSide(Quantity quantity, ThreeSides side) {
            if (quantity == Quantity.VOLTAGE && side != null) {
                throw new IllegalArgumentException("A voltage carries no branch side, was given " + side);
            }
            return side;
        }
    }
}
