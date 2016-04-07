/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline;

import eu.itesla_project.modules.offline.SecurityIndexSynthesis;
import eu.itesla_project.modules.rules.RuleId;
import eu.itesla_project.modules.rules.SecurityRule;
import eu.itesla_project.offline.monitoring.BusyCoresSeries;

import java.util.Collection;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface OfflineApplicationListener {

    void onWorkflowCreation(OfflineWorkflowStatus status);

    void onWorkflowRemoval(String workflowId);

    void onBusyCoresUpdate(BusyCoresSeries busyCoresSeries);

    void onWorkflowStatusChange(OfflineWorkflowStatus status);

    void onWorkflowListChange(Collection<OfflineWorkflowStatus> statuses);

    void onSamplesChange(String workflowId, Collection<SampleSynthesis> samples);

    @Deprecated
    void onSecurityIndexesChange(String workflowId, SecurityIndexSynthesis synthesis);

    void onSecurityRulesChange(String workflowId, Collection<RuleId> ruleIds);

    void onSecurityRulesProgress(String workflowId, Float progress);

    void onSecurityRuleDescription(String workflowId, SecurityRule description);

}
