/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Yichen TANG {@literal <yichen.tang at rte-france.com>}
 */
public class ComputationParametersBuilder {

    private final Map<String, Long> timeoutMap = new HashMap<>();

    private final Map<String, Long> deadlineMap = new HashMap<>();

    public ComputationParametersBuilder setTimeout(String cmdId, long seconds) {
        Objects.requireNonNull(cmdId);
        Preconditions.checkArgument(seconds > 0, "Timeout must be positive.");
        timeoutMap.put(cmdId, seconds);
        return this;
    }

    public ComputationParametersBuilder setDeadline(String cmdId, long seconds) {
        Objects.requireNonNull(cmdId);
        Preconditions.checkArgument(seconds > 0, "Deadline must be positive.");
        deadlineMap.put(cmdId, seconds);
        return this;
    }

    public ComputationParameters build() {
        return new ComputationParametersImpl(timeoutMap, deadlineMap);
    }
}
