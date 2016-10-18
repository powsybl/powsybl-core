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
import eu.itesla_project.modules.rules.expr.ExpressionFlatPrinter;
import eu.itesla_project.modules.rules.expr.ExpressionGraphvizPrinter;
import eu.itesla_project.modules.rules.expr.ExpressionNode;
import eu.itesla_project.modules.rules.expr.ExpressionTreePrinter;
import eu.itesla_project.simulation.securityindexes.SecurityIndexType;
import org.apache.commons.cli.CommandLine;

import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class PrintSecurityRuleTool implements Tool {

    @Override
    public Command getCommand() {
        return PrintSecurityRuleCommand.INSTANCE;
    }

    @Override
    public void run(CommandLine line) throws Exception {
        OfflineConfig config = OfflineConfig.load();
        String rulesDbName = line.hasOption("rules-db-name") ? line.getOptionValue("rules-db-name") : OfflineConfig.DEFAULT_RULES_DB_NAME;
        RulesDbClient rulesDb = config.getRulesDbClientFactoryClass().newInstance().create(rulesDbName);
        String workflowId = line.getOptionValue("workflow");
        RuleAttributeSet attributeSet = RuleAttributeSet.valueOf(line.getOptionValue("attribute-set"));
        String contingency = line.getOptionValue("contingency");
        SecurityIndexType indexType = SecurityIndexType.valueOf(line.getOptionValue("index-type"));
        ExpressionPrintFormat format = ExpressionPrintFormat.ASCII_FLAT;
        if (line.hasOption("format")) {
            format = ExpressionPrintFormat.valueOf(line.getOptionValue("format"));
        }
        double purityThreshold = PrintSecurityRuleCommand.DEFAULT_PURITY_THRESHOLD;
        if (line.hasOption("purity-threshold")) {
            purityThreshold = Double.parseDouble(line.getOptionValue("purity-threshold"));
        }
        List<SecurityRule> rules = rulesDb.getRules(workflowId, attributeSet, contingency, indexType);
        if (rules.isEmpty()) {
            throw new RuntimeException("Security rule not found");
        }
        SecurityRule rule = rules.get(0);
        SecurityRuleExpression securityRuleExpression = rule.toExpression(purityThreshold);
        if (securityRuleExpression.getStatus() == SecurityRuleStatus.ALWAYS_SECURE
                || securityRuleExpression.getStatus() == SecurityRuleStatus.ALWAYS_UNSECURE) {
            System.out.println(securityRuleExpression.getStatus());
        }
        ExpressionNode condition = securityRuleExpression.getCondition();
        String str;
        switch (format) {
            case ASCII_FLAT:
                str = ExpressionFlatPrinter.toString(condition);
                break;
            case ASCII_TREE:
                str = ExpressionTreePrinter.toString(condition);
                break;
            case GRAPHVIZ_TREE:
                str = ExpressionGraphvizPrinter.toString(condition);
                break;
            default:
                throw new AssertionError();
        }
        System.out.println(str);
    }

}
