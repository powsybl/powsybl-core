/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.CurrentLimits;
import java.util.Collection;
import java.util.Objects;
import java.util.TreeMap;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CurrentLimitsImpl implements CurrentLimits {

    private final float permanentLimit;

    private final TreeMap<Integer, TemporaryLimit> temporaryLimits;

    static class TemporaryLimitImpl implements TemporaryLimit {

        private final String name;

        private final float value;

        private final int acceptableDuration;

        private boolean fictitious;

        TemporaryLimitImpl(String name, float value, int acceptableDuration, boolean fictitious) {
            this.name = Objects.requireNonNull(name);
            this.value = value;
            this.acceptableDuration = acceptableDuration;
            this.fictitious = fictitious;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public float getValue() {
            return value;
        }

        @Override
        public int getAcceptableDuration() {
            return acceptableDuration;
        }

        @Override
        public boolean isFictitious() {
            return fictitious;
        }
    }

    CurrentLimitsImpl(float permanentLimit, TreeMap<Integer, TemporaryLimit> temporaryLimits) {
        this.permanentLimit = permanentLimit;
        this.temporaryLimits = temporaryLimits;
    }

    @Override
    public float getPermanentLimit() {
        return permanentLimit;
    }

    @Override
    public Collection<TemporaryLimit> getTemporaryLimits() {
        return temporaryLimits.values();
    }

    @Override
    public TemporaryLimit getTemporaryLimit(int acceptableDuration) {
        return temporaryLimits.get(acceptableDuration);
    }

    @Override
    public float getTemporaryLimitValue(int acceptableDuration) {
        TemporaryLimit tl = getTemporaryLimit(acceptableDuration);
        return tl != null ? tl.getValue() : Float.NaN;
    }
}
