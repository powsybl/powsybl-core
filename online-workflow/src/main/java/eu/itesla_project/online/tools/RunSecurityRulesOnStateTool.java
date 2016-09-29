/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.tools;

import com.google.auto.service.AutoService;
import com.google.common.base.Function;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.histo.HistoDbAttributeId;
import eu.itesla_project.modules.histo.IIDM2DB;
import eu.itesla_project.modules.online.OnlineConfig;
import eu.itesla_project.modules.online.OnlineDb;
import eu.itesla_project.modules.online.OnlineWorkflowParameters;
import eu.itesla_project.modules.rules.*;
import eu.itesla_project.simulation.securityindexes.SecurityIndexType;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.Table;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@AutoService(Tool.class)
public class RunSecurityRulesOnStateTool implements Tool {
	
	enum CheckStatus {
        OK,
        NOK,
        NA // not available
    }

	private static Command COMMAND = new Command() {
		
		@Override
		public String getName() {
			return "run-security-rules-on-state";
		}

		@Override
		public String getTheme() {
			return Themes.ONLINE_WORKFLOW;
		}

		@Override
		public String getDescription() {
			return "Run security rules computed by an offline workflow on a stored state of an online workflow";
		}

		@Override
		public Options getOptions() {
			Options options = new Options();
			options.addOption(Option.builder().longOpt("workflow")
	                .desc("the online workflow id")
	                .hasArg()
	                .required()
	                .argName("ID")
	                .build());
			options.addOption(Option.builder().longOpt("state")
	                .desc("the state id")
	                .hasArg()
	                .required()
	                .argName("STATE")
	                .build());
			options.addOption(Option.builder().longOpt("offline-workflow")
	                .desc("the offline workflow id")
	                .hasArg()
	                .argName("OFFID")
	                .build());
			options.addOption(Option.builder().longOpt("wca")
	                .desc("get results of wca rules (monte_carlo if not specified)")
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
		String workflowId = line.getOptionValue("workflow");
		Integer stateId = Integer.valueOf(line.getOptionValue("state"));
		System.out.println("loading state " + stateId + " of workflow " + workflowId + " from the online db ...");
        OnlineConfig config = OnlineConfig.load();
		OnlineDb onlinedb = config.getOnlineDbFactoryClass().newInstance().create();
        // load the network
        Network network = onlinedb.getState(workflowId, stateId);
        if ( network != null ) {
        	OnlineWorkflowParameters parameters = onlinedb.getWorkflowParameters(workflowId);
        	String offlineWorkflowId = parameters.getOfflineWorkflowId();
        	if (line.hasOption("offline-workflow"))
        		offlineWorkflowId = line.getOptionValue("offline-workflow");
        	System.out.println("checking state " + stateId + " of workflow " + workflowId + " against rules of offline workflow " + offlineWorkflowId + " ...");
        	RulesDbClient rulesDb = config.getRulesDbClientFactoryClass().newInstance().create("rulesdb");
        	RuleAttributeSet attributeSet = RuleAttributeSet.MONTE_CARLO;
        	if ( line.hasOption("wca"))
        		attributeSet = RuleAttributeSet.WORST_CASE;
        	double purityThreshold = parameters.getRulesPurityThreshold();
        	// get rules from db
            Collection<RuleId> ruleIds = rulesDb.listRules(offlineWorkflowId, attributeSet);
            // TODO filter rules that does not apply to the network
            // ...
            // sort rules per contingency
            Multimap<String, RuleId> ruleIdsPerContingency = Multimaps.index(ruleIds, new Function<RuleId, String>() {
                @Override
                public String apply(RuleId ruleId) {
                    return ruleId.getSecurityIndexId().getContingencyId();
                }
            });
            Map<HistoDbAttributeId, Object> values = IIDM2DB.extractCimValues(network, new IIDM2DB.Config(null, false)).getSingleValueMap();
            SecurityIndexType[] securityIndexTypes = parameters.getSecurityIndexes() == null ? SecurityIndexType.values()
                    : parameters.getSecurityIndexes().toArray(new SecurityIndexType[parameters.getSecurityIndexes().size()]);
            Table table = new Table(1 + securityIndexTypes.length, BorderStyle.CLASSIC_WIDE);
            table.addCell("Contingency");
            for (SecurityIndexType securityIndexType : securityIndexTypes) {
                table.addCell(securityIndexType.toString());
            }
            // check rules
            for (Map.Entry<String, Collection<RuleId>> entry : ruleIdsPerContingency.asMap().entrySet()) {
                String contingencyId = entry.getKey();
                table.addCell(contingencyId);
                Map<SecurityIndexType, CheckStatus> checkStatus = new EnumMap<>(SecurityIndexType.class);
                for (SecurityIndexType securityIndexType : securityIndexTypes) {
                    checkStatus.put(securityIndexType, CheckStatus.NA);
                }
                for (RuleId ruleId : entry.getValue()) {
                    List<SecurityRule> rules = rulesDb.getRules(offlineWorkflowId, attributeSet, contingencyId, ruleId.getSecurityIndexId().getSecurityIndexType());
                    if (rules.size() > 0) {
                        SecurityRule rule = rules.get(0);
                        SecurityRuleExpression securityRuleExpression = rule.toExpression(purityThreshold);
                        boolean ok = securityRuleExpression.check(values).isSafe();
                        checkStatus.put(rule.getId().getSecurityIndexId().getSecurityIndexType(), ok ? CheckStatus.OK : CheckStatus.NOK);
                    }
                }
                for (SecurityIndexType securityIndexType : securityIndexTypes) {
                    table.addCell(checkStatus.get(securityIndexType).name());
                }
            }
            System.out.println(table.render());
        } else {
        	System.out.println("no state " + stateId + " of workflow " + workflowId + " stored in the online db");
        }
		onlinedb.close();
	}
	
}
