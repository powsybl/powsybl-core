/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline;

import eu.itesla_project.modules.offline.SecurityIndexSynthesis;
import eu.itesla_project.modules.offline.SecurityIndexSynthesis.SecurityBalance;
import eu.itesla_project.modules.rules.RuleId;
import eu.itesla_project.modules.rules.SecurityRule;
import eu.itesla_project.simulation.securityindexes.SecurityIndexType;
import eu.itesla_project.offline.monitoring.BusyCoresSeries;

import java.util.Collection;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class OfflineApplicationPrinter implements OfflineApplicationListener {

    @Override
    public void onWorkflowCreation(OfflineWorkflowStatus status) {
        System.out.println("onWorkflowCreation " + status);
    }

    @Override
    public void onWorkflowRemoval(String workflowId) {
        System.out.println("onWorkflowRemove " + workflowId);
    }

    @Override
    public void onBusyCoresUpdate(BusyCoresSeries busyCores) {
        System.out.println("onBusyCoresUpdate " + busyCores);
    }

    @Override
    public void onWorkflowListChange(Collection<OfflineWorkflowStatus> statuses) {
        System.out.println("onWorkflowListChange");
        for(OfflineWorkflowStatus offlineWorkflowStatus : statuses) {
            System.out.println(offlineWorkflowStatus);
        }
    }

    @Override
    public void onWorkflowStatusChange(OfflineWorkflowStatus status) {
        System.out.println("onWorkflowStatusChange " + status);
    }

    @Override
    public void onSamplesChange(String workflowId, Collection<SampleSynthesis> samples) {
        System.out.println("onSamplesChange " + workflowId);
        for (SampleSynthesis sample : samples) {
            System.out.println("    " + sample.getId() + " " + sample.getLastTaskEvent().getTaskType() + " " + sample.getLastTaskEvent().getTaskStatus());
        }
    }

    @Override
    public void onSecurityIndexesChange(String workflowId, SecurityIndexSynthesis synthesis) {
        System.out.println("onSecurityIndexesChange " + workflowId);
        for (String contingencyId : synthesis.getContingencyIds()) {
            System.out.println("    " + contingencyId);
            for (SecurityIndexType securityIndexType : synthesis.getSecurityIndexTypes()) {
                SecurityBalance balance = synthesis.getSecurityBalance(contingencyId, securityIndexType);
                System.out.println("        " + securityIndexType + " " + balance.getStableCount() + " " + balance.getUnstableCount());
            }
        }
    }

    @Override
    public void onSecurityRulesChange(String workflowId, Collection<RuleId> ruleIds) {
        System.out.println("onSecurityRulesChange " + workflowId + ", " + ruleIds.size());
    }

    @Override
    public void onSecurityRuleDescription(String workflowId, SecurityRule rule) {
        System.out.println("onSecurityRuleDescription " + workflowId + ", " + rule);
    }

    @Override
    public void onSecurityRulesProgress(String workflowId, Float progress) {
        System.out.println("onSecurityRulesChange " + workflowId + ", " + progress);
    }
}
