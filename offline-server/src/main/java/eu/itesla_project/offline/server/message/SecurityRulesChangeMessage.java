/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.server.message;

import eu.itesla_project.modules.rules.RuleId;
import java.util.Collection;
import javax.json.stream.JsonGenerator;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityRulesChangeMessage extends Message {

    private final String workflowId;
    private final Collection<RuleId> ruleIds;

    public SecurityRulesChangeMessage(String workflowId, Collection<RuleId> ruleIds) {
        this.workflowId = workflowId;
        this.ruleIds = ruleIds;
    }

    @Override
    protected String getType() {
        return "securityRulesChange";
    }

    @Override
    public void toJson(JsonGenerator generator) {
        generator.write("workflowId", workflowId);
        generator.writeStartArray("ruleIds");
        for(RuleId ruleId : ruleIds) {
            generator.writeStartObject();
            generator.write("attributeSet", ruleId.getAttributeSet().toString());
            generator.writeStartObject("securityIndexId");
            generator.write("contingencyId", ruleId.getSecurityIndexId().getContingencyId());
            generator.writeStartObject("securityIndexType");
            generator.write("name", ruleId.getSecurityIndexId().getSecurityIndexType().name());
            generator.write("label", ruleId.getSecurityIndexId().getSecurityIndexType().getLabel());
            generator.write("dynamic", ruleId.getSecurityIndexId().getSecurityIndexType().isDynamic());
            generator.writeEnd();
            generator.writeEnd();
            generator.writeEnd();
        }
        generator.writeEnd();
    }

}
