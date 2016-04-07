/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.CurrentLimits;
import java.util.Collection;
import java.util.TreeMap;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class CurrentLimitsImpl implements CurrentLimits {

    private final float permanentLimit;

    private final TreeMap<Integer, TemporaryLimit> temporaryLimits;

    static class TemporaryLimitImpl implements TemporaryLimit {

        private final float limit;

        private final int acceptableDuration;

        TemporaryLimitImpl(float limit, int acceptableDuration) {
            this.limit = limit;
            this.acceptableDuration = acceptableDuration;
        }

        @Override
        public float getLimit() {
            return limit;
        }

        @Override
        public int getAcceptableDuration() {
            return acceptableDuration;
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

}
