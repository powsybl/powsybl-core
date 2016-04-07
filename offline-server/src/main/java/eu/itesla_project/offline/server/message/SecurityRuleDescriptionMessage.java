/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.server.message;

import eu.itesla_project.modules.rules.SecurityRule;

import javax.json.stream.JsonGenerator;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityRuleDescriptionMessage extends Message {

    private final String workflowId;
    private final SecurityRule securityRule;

    public SecurityRuleDescriptionMessage(String workflowId, SecurityRule securityRule) {
        this.workflowId = workflowId;
        this.securityRule = securityRule;
    }

    @Override
    protected String getType() {
        return "securityRuleComputation";
    }

    @Override
    public void toJson(JsonGenerator generator) {
        generator.write("workflowId", workflowId);
        generator.writeStartObject("ruleDescription");
        //TODO
        generator.writeEnd();
    }

}
