/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation;

import com.powsybl.commons.extensions.Extendable;
import com.powsybl.commons.extensions.Extension;

import java.util.Collections;
import java.util.OptionalLong;

/**
 * @author Yichen TANG {@literal <yichen.tang at rte-france.com>}
 */
public interface ComputationParameters extends Extendable<ComputationParameters> {

    /**
     * Returns an optional describing the execution time limit in seconds, the waiting time in queue is not included. See
     * differences with {@link #getDeadline(String)}
     * @param commandId id of {@link Command}
     * @return an optional describing seconds
     */
    OptionalLong getTimeout(String commandId);

    /**
     * Returns an optional describing the total time limit in seconds, the waiting time in queue is included. See differences
     * with {@link #getTimeout(String)}
     * @param commandId id of {@link Command}
     * @return an optional describing seconds
     */
    OptionalLong getDeadline(String commandId);

    /**
     * @return an empty {@link ComputationParameters}, but it supports plugin. See more {@link Extension}
     */
    static ComputationParameters empty() {
        return new ComputationParametersImpl(Collections.emptyMap(), Collections.emptyMap());
    }
}
