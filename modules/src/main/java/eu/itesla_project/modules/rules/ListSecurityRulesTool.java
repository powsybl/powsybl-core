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
import eu.itesla_project.modules.rules.expr.ExpressionStatistics;
import eu.itesla_project.modules.securityindexes.SecurityIndexId;
import org.apache.commons.cli.CommandLine;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.Table;

import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class ListSecurityRulesTool implements Tool {

    @Override
    public Command getCommand() {
        return ListSecurityRulesCommand.INSTANCE;
    }

    @Override
    public void run(CommandLine line) throws Exception {
        OfflineConfig config = OfflineConfig.load();
        String rulesDbName = line.hasOption("rules-db-name") ? line.getOptionValue("rules-db-name") : OfflineConfig.DEFAULT_RULES_DB_NAME;
        RulesDbClient rulesDb = config.getRulesDbClientFactoryClass().newInstance().create(rulesDbName);
        String workflowId = line.getOptionValue("workflow");
        boolean addInfos = line.hasOption("add-infos");
        double purityThreshold = CheckSecurityCommand.DEFAULT_PURITY_THRESHOLD;
        if (line.hasOption("purity-threshold")) {
            purityThreshold = Double.parseDouble(line.getOptionValue("purity-threshold"));
        }
        Set<RuleId> ruleIds = new TreeSet<>(rulesDb.listRules(workflowId, null));
        Table table = new Table(addInfos ? 7 : 3, BorderStyle.CLASSIC_WIDE);
        table.addCell("Contingency ID");
        table.addCell("Security index type");
        table.addCell("Attribute Set");
        if (addInfos) {
            table.addCell("Status");
            table.addCell("Attribute count");
            table.addCell("Convex set count");
            table.addCell("Inequality count");
        }
        for (RuleId ruleId : ruleIds) {
            SecurityIndexId securityIndexId = ruleId.getSecurityIndexId();
            table.addCell(securityIndexId.getContingencyId());
            table.addCell(securityIndexId.getSecurityIndexType().toString());
            table.addCell(ruleId.getAttributeSet().toString());
            if (addInfos) {
                SecurityRule rule = rulesDb.getRules(workflowId, ruleId.getAttributeSet(), securityIndexId.getContingencyId(), securityIndexId.getSecurityIndexType()).get(0);
                SecurityRuleExpression ruleExpr = rule.toExpression(purityThreshold);
                table.addCell(ruleExpr.getStatus().toString());
                if (ruleExpr.getStatus() == SecurityRuleStatus.SECURE_IF) {
                    ExpressionStatistics statistics = ExpressionStatistics.compute(ruleExpr.getCondition());
                    table.addCell(Integer.toString(statistics.getAttributeCount()));
                    table.addCell(Integer.toString(statistics.getConvexSetCount()));
                    table.addCell(Integer.toString(statistics.getInequalityCount()));
                } else {
                    table.addCell("");
                    table.addCell("");
                    table.addCell("");
                }
            }
        }
        System.out.println(table.render());
    }

}
