/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.server.message;

import eu.itesla_project.offline.OfflineWorkflowStatus;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.json.stream.JsonGenerator;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class WorkflowListMessage extends Message {

    private final List<OfflineWorkflowStatus> offlineWorkflowStatuses;

    public WorkflowListMessage(Collection<OfflineWorkflowStatus> offlineWorkflowStatuses) {
        this.offlineWorkflowStatuses = new ArrayList<>(Objects.requireNonNull(offlineWorkflowStatuses));
    }

    @Override
    protected String getType() {
        return "workflowList";
    }

    @Override
    public void toJson(JsonGenerator generator) {
        generator.writeStartArray("workflowStatuses");
        for (OfflineWorkflowStatus status : offlineWorkflowStatuses) {
            generator.writeStartObject();
            new WorkflowStatusMessage(status).toJson(generator);
            generator.writeEnd();
        }
        generator.writeEnd();
    }

}
