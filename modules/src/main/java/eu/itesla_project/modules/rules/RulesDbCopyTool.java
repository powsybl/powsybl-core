/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.rules;

import com.google.auto.service.AutoService;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.modules.offline.OfflineConfig;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class RulesDbCopyTool implements Tool {

    private static final Command COMMAND = new Command() {

        @Override
        public String getName() {
            return "copy-rules";
        }

        @Override
        public String getTheme() {
            return "Security rules DB";
        }

        @Override
        public String getDescription() {
            return "copy content of a rules DB to another one";
        }

        @Override
        @SuppressWarnings("static-access")
        public Options getOptions() {
            Options options = new Options();
            options.addOption(Option.builder().longOpt("workflow")
                    .desc("the workflow id")
                    .hasArg()
                    .required()
                    .argName("ID")
                    .build());
            options.addOption(Option.builder().longOpt("target-rules-db-class")
                    .desc("the target rules db class")
                    .hasArg()
                    .required()
                    .argName("TARGET_CLASS")
                    .build());
            options.addOption(Option.builder().longOpt("source-rules-db-name")
                    .desc("the source rules db name (default is " + OfflineConfig.DEFAULT_RULES_DB_NAME + ")")
                    .hasArg()
                    .argName("SOURCE_NAME")
                    .build());
            options.addOption(Option.builder().longOpt("target-rules-db-name")
                    .desc("the rules target db name (default is " + OfflineConfig.DEFAULT_RULES_DB_NAME + ")")
                    .hasArg()
                    .argName("TARGET_NAME")
                    .build());

            return options;
        }

        @Override
        public String getUsageFooter() {
            return null;
        }

    };

    @Override
    public Command getCommand() {
        return COMMAND;
    }

    @Override
    public void run(CommandLine line) throws Exception {
        OfflineConfig config = OfflineConfig.load();
        String sourceRulesDbName = line.hasOption("source-rules-db-name") ? line.getOptionValue("source-rules-db-name") : OfflineConfig.DEFAULT_RULES_DB_NAME;
        String targetRulesDbName = line.hasOption("target-rules-db-name") ? line.getOptionValue("target-rules-db-name") : OfflineConfig.DEFAULT_RULES_DB_NAME;
        Class<? extends RulesDbClientFactory> targetRulesDbClass = Class.forName(line.getOptionValue("target-rules-db-class")).asSubclass(RulesDbClientFactory.class);
        RulesDbClient sourceRulesDb = config.getRulesDbClientFactoryClass().newInstance().create(sourceRulesDbName);
        RulesDbClient targetRulesDb = targetRulesDbClass.newInstance().create(targetRulesDbName);
        String workflowId = line.getOptionValue("workflow");
        List<RuleId> ruleIds = new ArrayList<>(sourceRulesDb.listRules(workflowId, null));
        for (int i = 0; i < ruleIds.size(); i++) {
            RuleId ruleId = ruleIds.get(i);
            SecurityRule rule = sourceRulesDb.getRules(workflowId,
                                                          ruleId.getAttributeSet(),
                                                          ruleId.getSecurityIndexId().getContingencyId(),
                                                          ruleId.getSecurityIndexId().getSecurityIndexType()).get(0);
            System.out.println("copying rule " + (i + 1) + "/" + ruleIds.size() + ": " + ruleId);
            targetRulesDb.updateRule(rule);
        }
    }

}
