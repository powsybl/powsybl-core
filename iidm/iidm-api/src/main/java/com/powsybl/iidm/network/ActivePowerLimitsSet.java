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
public interface ActivePowerLimitsSet extends LoadingLimitsSet<ActivePowerLimits> {

    ActivePowerLimitsSet EMPTY = new ActivePowerLimitsSet() {

        @Override
        public ActivePowerLimits getLimits(String id) {
            return null;
        }

        @Override
        public Optional<ActivePowerLimits> getActiveLimits() {
            return Optional.empty();
        }

        @Override
        public Collection<ActivePowerLimits> getLimits() {
            return Collections.emptySet();
        }

        @Override
        public void remove() {
            // do nothing
        }
    };
}
