/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline;

import eu.itesla_project.modules.offline.OfflineWorkflowCreationParameters;
import eu.itesla_project.modules.offline.SecurityIndexSynthesis;
import eu.itesla_project.modules.rules.RuleId;
import eu.itesla_project.modules.rules.SecurityRuleExpression;
import java.util.Map;

/**
 * Workflows administration API
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface OfflineApplication extends AutoCloseable {

    Map<String, OfflineWorkflowStatus> listWorkflows();

    String createWorkflow(String workflowId, OfflineWorkflowCreationParameters parameters);

    OfflineWorkflowCreationParameters getWorkflowParameters(String workflowId);

    void removeWorkflow(String workflowId);

    void startWorkflow(String workflowId, OfflineWorkflowStartParameters startParameters);

    void stopWorkflow(String workflowId);

    void computeSecurityRules(String workflowId);

    SecurityIndexSynthesis getSecurityIndexesSynthesis(String workflowId);
    
    void getSecurityRules(String workflowId);
    
    SecurityRuleExpression getSecurityRuleExpression(String workflowId, RuleId ruleId);
    
    void addListener(OfflineApplicationListener l);

    void removeListener(OfflineApplicationListener l);

    void refreshAll();

    void stopApplication();
}
