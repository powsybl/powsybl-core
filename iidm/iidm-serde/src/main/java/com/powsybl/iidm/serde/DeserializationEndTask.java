/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class DeserializationEndTask {
    public enum Step {
        BEFORE_EXTENSIONS,
        AFTER_EXTENSIONS
    }

    private final Step step;
    private final Runnable task;
    private boolean processed;

    public DeserializationEndTask(Step step, Runnable task) {
        this.step = step;
        this.task = task;
        this.processed = false;
    }

    public Step getStep() {
        return step;
    }

    public Runnable getTask() {
        return task;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }
}
