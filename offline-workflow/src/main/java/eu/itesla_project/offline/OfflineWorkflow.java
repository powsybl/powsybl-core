/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline;

import eu.itesla_project.modules.offline.OfflineWorkflowCreationParameters;


/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface OfflineWorkflow {

    static String getValidationDir(String id) {
        return "offline/" + id;
    }

    String getId();

    void start(OfflineWorkflowStartParameters startParameters) throws Exception;
    
    void stop();

    void computeSecurityRules() throws Exception;
            
    OfflineWorkflowStatus getStatus();

    OfflineWorkflowCreationParameters getCreationParameters();
    
    void addListener(OfflineWorkflowListener listener);

    void removeListener(OfflineWorkflowListener listener);

    void addSynthesisListener(OfflineWorkflowSynthesisListener listener);

    void removeSynthesisListener(OfflineWorkflowSynthesisListener listener);

    void notifySynthesisListeners();
}
