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

    private final List<Area> areas;
    private final List<ElementObservability> elementObservabilities;

    public ObservabilityResultImpl(List<Area> areas, List<ElementObservability> elementObservabilities) {
        this.areas = List.copyOf(Objects.requireNonNull(areas));
        this.elementObservabilities = List.copyOf(Objects.requireNonNull(elementObservabilities));
    }

    @Override
    public List<Area> getAreas() {
        return areas;
    }

    @Override
    public Optional<Area> getArea(String busId) {
        Objects.requireNonNull(busId);
        return areas.stream().filter(a -> a.getBusIds().contains(busId)).findFirst();
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

    public static class AreaImpl implements Area {

        private final int areaNumber;
        private final AreaStatus status;
        private final Set<String> busIds;

        public AreaImpl(int areaNumber, AreaStatus status, Set<String> busIds) {
            this.areaNumber = areaNumber;
            this.status = Objects.requireNonNull(status);
            this.busIds = Set.copyOf(Objects.requireNonNull(busIds));
        }

        @Override
        public int getAreaNumber() {
            return areaNumber;
        }

        @Override
        public AreaStatus getStatus() {
            return status;
        }

        @Override
        public Set<String> getBusIds() {
            return busIds;
        }
    }

    public static class ElementObservabilityImpl implements ElementObservability {

        private final String elementId;
        private final boolean observable;
        private final List<Quality> qualities;

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

    public static class QualityImpl implements Quality {

        private final Quantity quantity;
        private final ThreeSides side;
        private final double standardDeviation;
        private final Boolean redundant;

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
