/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.tools;

import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.commons.tools.Command;
import com.google.auto.service.AutoService;
import eu.itesla_project.offline.OfflineApplication;
import eu.itesla_project.offline.OfflineWorkflowStatus;
import eu.itesla_project.offline.RemoteOfflineApplicationImpl;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormat;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.Table;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class ListOfflineWorkflowsTool implements Tool {

    @Override
    public Command getCommand() {
        return ListOfflineWorkflowsCommand.INSTANCE;
    }

    @Override
    public void run(CommandLine line) throws Exception {
        try (OfflineApplication app = new RemoteOfflineApplicationImpl()) {
            Map<String, OfflineWorkflowStatus> statuses = app.listWorkflows();
            Table table = new Table(4, BorderStyle.CLASSIC_WIDE);
            table.addCell("ID");
            table.addCell("Running");
            table.addCell("Step");
            table.addCell("Time");
            for (Map.Entry<String, OfflineWorkflowStatus> entry : statuses.entrySet()) {
                String workflowId = entry.getKey();
                OfflineWorkflowStatus status = entry.getValue();
                Duration remaining = null;
                if (status.getStartTime() != null) {
                    remaining = Duration.millis(status.getStartParameters().getDuration() * 60 * 1000)
                            .minus(new Duration(status.getStartTime(), DateTime.now()));
                }
                table.addCell(workflowId);
                table.addCell(Boolean.toString(status.isRunning()));
                table.addCell(status.getStep() != null ? status.getStep().toString() : "");
                table.addCell(remaining != null ? PeriodFormat.getDefault().print(remaining.toPeriod()) : "");
            }
            System.out.println(table.render());
        }
    }

}
