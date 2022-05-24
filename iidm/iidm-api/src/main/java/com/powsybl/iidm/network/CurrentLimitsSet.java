/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public interface CurrentLimitsSet extends LoadingLimitsSet<CurrentLimits> {

    CurrentLimitsSet EMPTY = new CurrentLimitsSet() {

        @Override
        public CurrentLimits getLimits(String id) {
            return null;
        }

        @Override
        public Optional<CurrentLimits> getActivatedLimits() {
            return Optional.empty();
        }

        @Override
        public void setActivatedLimits(String id) {
            throw new UnsupportedOperationException("Set has not been initialized");
        }

        @Override
        public Collection<CurrentLimits> getLimits() {
            return Collections.emptySet();
        }

        @Override
        public void remove() {
            // do nothing
        }
    };
}
