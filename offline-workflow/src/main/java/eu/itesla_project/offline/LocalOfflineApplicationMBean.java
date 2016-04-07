/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface LocalOfflineApplicationMBean extends OfflineApplication {

    String BEAN_NAME = "eu.itesla_project.offline:type=LocalOfflineApplicationMBean";

    enum Attribute {
        /* Resources management */
        BUSY_CORES,
        /* Workflow lifecycle */
        WORKFLOW_CREATION, WORKFLOW_REMOVAL, WORKFLOW_LIST, WORKFLOW_STATUS,
        /* Workflow details */
        SAMPLES,
        SECURITY_RULES,             // existing security rule ids for the workflow
        SECURITY_RULES_PROGRESS,    // security rules computation progress
        SECURITY_RULE_DESCRIPTION;  // security rule details
    }
    
    void ping();

}
