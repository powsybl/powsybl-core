/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.server.message;

import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.modules.offline.OfflineWorkflowCreationParameters;
import eu.itesla_project.offline.OfflineWorkflowStartParameters;
import eu.itesla_project.offline.OfflineWorkflowStatus;
import eu.itesla_project.offline.OfflineWorkflowStep;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Objects;
import javax.json.stream.JsonGenerator;
import org.joda.time.Interval;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class WorkflowStatusMessage extends Message {

    private final OfflineWorkflowStatus status;

    public WorkflowStatusMessage(OfflineWorkflowStatus offlineWorkflowStatus) {
        this.status = Objects.requireNonNull(offlineWorkflowStatus);
    }

    @Override
    protected String getType() {
        return "workflowStatus";
    }

    @Override
    public void toJson(JsonGenerator generator) {
        generator.write("workflowId", status.getWorkflowId());
        OfflineWorkflowStep step = status.getStep();
        OfflineWorkflowCreationParameters creationParameters = status.getCreationParameters();
        OfflineWorkflowStartParameters startParameters = status.getStartParameters();
        if (creationParameters != null) {
            if (creationParameters.getBaseCaseDate() != null) {
                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
                generator.write("baseCaseDate", df.format(creationParameters.getBaseCaseDate().toDate()));
            }
            if (creationParameters.getHistoInterval() != null) {
                DateFormat df = SimpleDateFormat.getDateInstance(DateFormat.SHORT);
                Interval interval = creationParameters.getHistoInterval();
                if (interval.getStart() != null) {
                    generator.write("intervalStart", df.format(interval.getStart().toDate()));
                }
                if (interval.getEnd() != null) {
                    generator.write("intervalStop", df.format(interval.getEnd().toDate()));
                }
            }
            if (creationParameters.getCountries() != null) {
                generator.writeStartArray("countries");
                for (Country country : creationParameters.getCountries()) {
                    generator.write(country.getName());
                }
                generator.writeEnd();
            }
        }
        if (step != null) {
            generator.write("step", step.name());
            generator.write("running", step.isRunning());
        } else {
            generator.write("step", "null");
            generator.write("running", false);
        }
        if (startParameters != null) {
            generator.write("duration", startParameters.getDuration());
            generator.write("startTime", status.getStartTime().toString());
        }

    }
}
