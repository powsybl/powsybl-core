/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline;

import eu.itesla_project.modules.offline.OfflineWorkflowCreationParameters;
import java.io.Serializable;
import java.util.Objects;
import org.joda.time.DateTime;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class OfflineWorkflowStatus implements Serializable {

    private final String workflowId;

    private final OfflineWorkflowStep step;
    
    private final DateTime startTime;
    
    private final OfflineWorkflowCreationParameters creationParameters;

    private final OfflineWorkflowStartParameters startParameters;

    public OfflineWorkflowStatus(String workflowId, OfflineWorkflowStep step, OfflineWorkflowCreationParameters creationParameters, OfflineWorkflowStartParameters startParameters) {
        this.workflowId = Objects.requireNonNull(workflowId);
        this.step = Objects.requireNonNull(step);
        this.creationParameters = Objects.requireNonNull(creationParameters);
        this.startParameters = Objects.requireNonNull(startParameters);
        this.startTime = new DateTime();
    }

    public OfflineWorkflowStatus(String workflowId, OfflineWorkflowStep step, OfflineWorkflowCreationParameters creationParameters) {
        this.workflowId = Objects.requireNonNull(workflowId);
        this.step = Objects.requireNonNull(step);
        if(!step.equals(OfflineWorkflowStep.IDLE) && !step.equals(OfflineWorkflowStep.SECURITY_RULES_COMPUTATION)) {
            throw new IllegalStateException("start parameters must be provided in initialization");
        }
        this.creationParameters = Objects.requireNonNull(creationParameters);
        this.startParameters = null;
        this.startTime = null;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public OfflineWorkflowStep getStep() {
        return step;
    }

    public boolean isRunning() {
        return step.isRunning();
    }

    public OfflineWorkflowCreationParameters getCreationParameters() {
        return creationParameters;
    }

    public OfflineWorkflowStartParameters getStartParameters() {
        return startParameters;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    @Override
    public String toString() {
        return "OfflineWorkflowStatus{" + "workflowId=" + workflowId + ", step=" + step + ", startTime=" + startTime + ", creationParameters=" + creationParameters + ", startParameters=" + startParameters + '}';
    }

}
