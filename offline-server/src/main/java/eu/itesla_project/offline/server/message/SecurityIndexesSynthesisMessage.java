/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.server.message;

import eu.itesla_project.modules.offline.SecurityIndexSynthesis;
import eu.itesla_project.modules.offline.SecurityIndexSynthesis.SecurityBalance;
import eu.itesla_project.simulation.securityindexes.SecurityIndexType;
import javax.json.stream.JsonGenerator;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityIndexesSynthesisMessage extends Message {

    private final SecurityIndexSynthesis synthesis;

    private final String workflowId;

    public SecurityIndexesSynthesisMessage(SecurityIndexSynthesis synthesis, String workflowId) {
        this.synthesis = synthesis;
        this.workflowId = workflowId;
    }

    @Override
    protected String getType() {
        return "securityIndexesSynthesis";
    }

    @Override
    public void toJson(JsonGenerator generator) {
        generator.write("workflowId", workflowId);
        generator.writeStartObject("securityIndexesSynthesis")
                .writeStartArray("securityIndexTypes");
        for (SecurityIndexType securityIndexType : synthesis.getSecurityIndexTypes()) {
            generator.write(securityIndexType.getLabel());
        }
        generator.writeEnd();
        generator.writeStartArray("contingencies");
        for (String contingencyId : synthesis.getContingencyIds()) {
            generator.writeStartObject()
                    .write("id", contingencyId)
                .writeStartObject("securityIndexes");
            for (SecurityIndexType securityIndexType : synthesis.getSecurityIndexTypes()) {
                SecurityBalance balance = synthesis.getSecurityBalance(contingencyId, securityIndexType);
                generator.writeStartObject(securityIndexType.getLabel())
                        .write("ok", balance.getStableCount())
                        .write("nok", balance.getUnstableCount())
                        .writeEnd();
            }
            generator.writeEnd()
                    .writeEnd();
        }
        generator.writeEnd()
                .writeEnd();
    }

}
