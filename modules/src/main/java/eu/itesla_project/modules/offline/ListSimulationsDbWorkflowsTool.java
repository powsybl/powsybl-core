/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.offline;

import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.commons.tools.Command;
import com.google.auto.service.AutoService;

import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.Table;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class ListSimulationsDbWorkflowsTool implements Tool {

    @Override
    public Command getCommand() {
        return ListSimulationsDbWorkflowsCommand.INSTANCE;
    }

    @Override
    public void run(CommandLine line) throws Exception {
        String simulationDbName = line.hasOption("simulation-db-name") ? line.getOptionValue("simulation-db-name") : OfflineConfig.DEFAULT_SIMULATION_DB_NAME;
        OfflineConfig config = OfflineConfig.load();
        OfflineDb offlineDb = config.getOfflineDbFactoryClass().newInstance().create(simulationDbName);
        List<String> workflowIds = offlineDb.listWorkflows();
        Table table = new Table(1, BorderStyle.CLASSIC_WIDE);
        table.addCell("ID");
        for (String workflowId : workflowIds) {
            table.addCell(workflowId);
        }
        System.out.println(table.render());
    }

}
