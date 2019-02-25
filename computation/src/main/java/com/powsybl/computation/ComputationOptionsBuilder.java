/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ComputationOptionsBuilder {

    private final Map<String, Integer> timeoutMap = new HashMap<>();

    private final Map<String, String> qosMap = new HashMap<>();

    private String qos;

    ComputationOptionsBuilder() {

    }

    public ComputationOptionsBuilder setDefaultQos(String qos) {
        this.qos = Objects.requireNonNull(qos);
        return this;
    }

    public ComputationOptionsBuilder setTimeout(String cmdId, int seconds) {
        Objects.requireNonNull(cmdId);
        Preconditions.checkArgument(seconds >= 0, "Timeout must be positive.");
        timeoutMap.put(cmdId, seconds);
        return this;
    }

    public ComputationOptionsBuilder setQos(String cmdId, String qos) {
        Objects.requireNonNull(cmdId);
        Objects.requireNonNull(qos);
        qosMap.put(cmdId, qos);
        return this;
    }

    public ComputationOptions build() {
        return new ComputationOptionsImpl(qos, timeoutMap, qosMap);
    }
}
