/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.mpi;

import com.powsybl.computation.ExecutionListener;
import com.powsybl.computation.ExecutionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ProfiledExecutionListener implements ExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfiledExecutionListener.class);

    private static final int WARNING_THRESHOLD = 10; // ms

    private final ExecutionListener delegate;

    ProfiledExecutionListener(ExecutionListener delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onExecutionStart(int fromExecutionIndex, int toExecutionIndex) {
        long start = System.currentTimeMillis();
        try {
            delegate.onExecutionStart(fromExecutionIndex, toExecutionIndex);
        } finally {
            long end = System.currentTimeMillis();
            if (end - start > WARNING_THRESHOLD) {
                LOGGER.warn("Slowness detected in ExecutionListener.onExecutionStart ({} ms)", end - start);
            }
        }
    }

    @Override
    public void onExecutionCompletion(int executionIndex) {
        long start = System.currentTimeMillis();
        try {
            delegate.onExecutionCompletion(executionIndex);
        } finally {
            long end = System.currentTimeMillis();
            if (end - start > WARNING_THRESHOLD) {
                LOGGER.warn("Slowness detected in ExecutionListener.onExecutionCompletion ({} ms)", end - start);
            }
        }
    }

    @Override
    public void onEnd(ExecutionReport report) {
        long start = System.currentTimeMillis();
        try {
            delegate.onEnd(report);
        } finally {
            long end = System.currentTimeMillis();
            if (end - start > WARNING_THRESHOLD) {
                LOGGER.warn("Slowness detected in ExecutionListener.onEnd ({} ms)", end - start);
            }
        }
    }

}
