/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class WorkflowStartContext {

    private final OfflineWorkflowStartParameters startParameters;

    private final long startMs = System.currentTimeMillis();

    private final AtomicInteger processedSamples = new AtomicInteger();

    public WorkflowStartContext(OfflineWorkflowStartParameters startParameters) {
        this.startParameters = startParameters;
    }

    public long getStartMs() {
        return startMs;
    }

    public OfflineWorkflowStartParameters getStartParameters() {
        return startParameters;
    }

    public void incrementProcessedSamples() {
        processedSamples.incrementAndGet();
    }

    public int getProcessedSamples() {
        return processedSamples.get();
    }
}
