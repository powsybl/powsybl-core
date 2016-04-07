/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline;

import eu.itesla_project.modules.rules.RuleId;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface OfflineWorkflowListener {

    void onWorkflowStatusChange(OfflineWorkflowStatus status);

    /**
     * Called each time a security rule is computed and stored in the rule db.
     * @param workflowId the workflow id
     * @param ruleId the rule id
     * @param ok true if rule computation succeed, false otherwise
     * @param percentComplete completion progress in percent
     */
    void onSecurityRuleStorage(String workflowId, RuleId ruleId, boolean ok, float percentComplete);

}
