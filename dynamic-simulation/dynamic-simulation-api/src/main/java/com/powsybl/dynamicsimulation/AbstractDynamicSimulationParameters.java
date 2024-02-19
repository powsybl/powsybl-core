/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation;

import com.powsybl.commons.extensions.AbstractExtendable;

import java.util.Map;
import java.util.Objects;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public abstract class AbstractDynamicSimulationParameters<T extends AbstractDynamicSimulationParameters<T>> extends AbstractExtendable<T> {

    public static final int DEFAULT_START_TIME = 0;
    public static final int DEFAULT_STOP_TIME = 10;

    private int startTime;
    private int stopTime;

    protected AbstractDynamicSimulationParameters(int startTime, int stopTime) {
        if (startTime < 0) {
            throw new IllegalStateException("Start time should be zero or positive");
        }
        if (stopTime <= startTime) {
            throw new IllegalStateException("Stop time should be greater than start time");
        }
        this.startTime = startTime;
        this.stopTime = stopTime;
    }

    protected AbstractDynamicSimulationParameters(AbstractDynamicSimulationParameters<T> other) {
        Objects.requireNonNull(other);
        startTime = other.startTime;
        stopTime = other.stopTime;
    }

    public int getStartTime() {
        return startTime;
    }

    /**
     *
     * @param startTime instant of time at which the dynamic simulation begins, in
     *                  seconds
     * @return
     */
    public T setStartTime(int startTime) {
        if (startTime < 0) {
            throw new IllegalStateException("Start time should be zero or positive");
        }
        this.startTime = startTime;
        return self();
    }

    public int getStopTime() {
        return stopTime;
    }

    /**
     *
     * @param stopTime instant of time at which the dynamic simulation ends, in
     *                 seconds
     * @return
     */
    public T setStopTime(int stopTime) {
        if (stopTime <= startTime) {
            throw new IllegalStateException("Stop time should be greater than start time");
        }
        this.stopTime = stopTime;
        return self();
    }

    protected abstract Map<String, Object> toMap();

    @Override
    public String toString() {
        return toMap().toString();
    }

    public abstract String getVersion();

    protected abstract T self();
}
