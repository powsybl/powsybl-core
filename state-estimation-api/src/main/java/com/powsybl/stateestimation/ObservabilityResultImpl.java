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

import java.util.List;
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
        return islands.stream().filter(i -> i.getBusIds().contains(busId)).findFirst();
    }

    @Override
    public List<ElementObservability> getElementObservabilities() {
        return elementObservabilities;
    }

    @Override
    public Optional<ElementObservability> getElementObservability(String elementId) {
        Objects.requireNonNull(elementId);
        return elementObservabilities.stream().filter(e -> e.getElementId().equals(elementId)).findFirst();
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
        private final List<Quality> qualities;

        /**
         * Neither {@code elementId} nor {@code qualities} may be null.
         *
         * @param elementId the identifier of the injection or branch
         * @param observable whether the measurements determine the state of the element
         * @param qualities one entry per quantity determined for the element
         */
        public ElementObservabilityImpl(String elementId, boolean observable, List<Quality> qualities) {
            this.elementId = Objects.requireNonNull(elementId);
            this.observable = observable;
            this.qualities = List.copyOf(Objects.requireNonNull(qualities));
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
        public List<Quality> getQualities() {
            return qualities;
        }
    }

    /** Immutable {@link Quality}. */
    public static class QualityImpl implements Quality {

        private final Quantity quantity;
        private final ThreeSides side;
        private final double standardDeviation;
        private final Boolean redundant;

        /**
         * {@code side} is null for an injection, and {@code redundant} is null when the estimation did
         * not establish whether the quantity is redundantly measured.
         *
         * @param quantity the measured quantity
         * @param side the branch side, or null for an injection
         * @param standardDeviation the standard deviation of the estimated quantity, in MW, MVar or kV
         * @param redundant whether more than one measurement determines the quantity, or null if that was not established
         */
        public QualityImpl(Quantity quantity, ThreeSides side, double standardDeviation, Boolean redundant) {
            this.quantity = Objects.requireNonNull(quantity);
            this.side = side;
            this.standardDeviation = standardDeviation;
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
        public double getStandardDeviation() {
            return standardDeviation;
        }

        @Override
        public Optional<Boolean> isRedundant() {
            return Optional.ofNullable(redundant);
        }
    }
}
