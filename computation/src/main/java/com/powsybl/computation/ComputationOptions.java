/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import java.util.Optional;
import java.util.OptionalLong;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public interface ComputationOptions {

    OptionalLong getTimeout(String commandId);

    Optional<String> getQos(String commandId);

    static ComputationOptions empty() {
        return new ComputationOptions() {
            @Override
            public OptionalLong getTimeout(String commandId) {
                return OptionalLong.empty();
            }

            @Override
            public Optional<String> getQos(String commandId) {
                return Optional.empty();
            }
        };
    }
}
