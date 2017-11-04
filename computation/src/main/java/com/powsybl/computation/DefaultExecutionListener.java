/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DefaultExecutionListener implements ExecutionListener {

    @Override
    public void onExecutionStart(int fromExecutionIndex, int toExecutionIndex) {
        // empty default implementation
    }

    @Override
    public void onExecutionCompletion(int executionIndex) {
        // empty default implementation
    }

    @Override
    public void onEnd(ExecutionReport report) {
        // empty default implementation
    }

}
