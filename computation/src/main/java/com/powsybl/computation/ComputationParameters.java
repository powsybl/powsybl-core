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
public interface ComputationParameters {

    /**
     * Returns an optional describing the execution time limit in seconds, the waiting time in queue is not inclued. See
     * differences with {@link #getDeadline(String)}
     * @param commandId id of {@link Command}
     * @return an optional describing seconds
     */
    OptionalLong getTimeout(String commandId);

    /**
     * Returns an optional describing the total time limmit in seconds, the waiting time in queue is included. See differences
     * with {@link #getTimeout(String)}
     * @param commandId id of {@link Command}
     * @return an optional describing seconds
     */
    OptionalLong getDeadline(String commandId);

    Optional<String> getQos(String commandId);

    static ComputationParameters empty() {
        return new ComputationParameters() {
            @Override
            public OptionalLong getTimeout(String commandId) {
                return OptionalLong.empty();
            }

            @Override
            public OptionalLong getDeadline(String commandId) {
                return OptionalLong.empty();
            }

            @Override
            public Optional<String> getQos(String commandId) {
                return Optional.empty();
            }
        };
    }
}
