/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.rules;

import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.commons.tools.Command;
import com.google.auto.service.AutoService;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.computation.local.LocalComputationManager;
import eu.itesla_project.modules.offline.MetricsDb;
import eu.itesla_project.modules.offline.OfflineDb;
import eu.itesla_project.modules.securityindexes.SecurityIndexId;
import eu.itesla_project.modules.securityindexes.SecurityIndexType;
import eu.itesla_project.modules.offline.OfflineConfig;
import org.apache.commons.cli.CommandLine;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class ComputeSecurityRuleTool implements Tool {

    @Override
    public Command getCommand() {
        return ComputeSecurityRuleCommand.INSTANCE;
    }

    @Override
    public void run(CommandLine line) throws Exception {
        String simulationDbName = line.hasOption("simulation-db-name") ? line.getOptionValue("simulation-db-name") : OfflineConfig.DEFAULT_SIMULATION_DB_NAME;
        String workflowId = line.getOptionValue("workflow");
        String rulesDbName = line.hasOption("rules-db-name") ? line.getOptionValue("rules-db-name") : OfflineConfig.DEFAULT_RULES_DB_NAME;
        String metricsDbName = line.hasOption("metrics-db-name") ? line.getOptionValue("metrics-db-name") : OfflineConfig.DEFAULT_METRICS_DB_NAME;
        RuleAttributeSet attributeSet = RuleAttributeSet.valueOf(line.getOptionValue("attribute-set"));
        String contingency = line.getOptionValue("contingency");
        SecurityIndexType indexType = SecurityIndexType.valueOf(line.getOptionValue("index-type"));
        OfflineConfig config = OfflineConfig.load();
        RulesDbClient rulesDb = config.getRulesDbClientFactoryClass().newInstance().create(rulesDbName);
        OfflineDb offlineDb = config.getOfflineDbFactoryClass().newInstance().create(simulationDbName);
        MetricsDb metricsDb = config.getMetricsDbFactoryClass().newInstance().create(metricsDbName);
        ComputationManager computationManager = new LocalComputationManager();
        RulesBuilder rulesBuilder = config.getRulesBuilderFactoryClass().newInstance().create(computationManager, offlineDb, metricsDb, rulesDb);
        rulesBuilder.build(workflowId, attributeSet, new SecurityIndexId(contingency, indexType));
    }

}
