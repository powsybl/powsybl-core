/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.histodb.client.impl;

import com.google.auto.service.AutoService;
import com.google.common.io.ByteStreams;
import eu.itesla_project.modules.rules.RuleId;
import eu.itesla_project.modules.rules.SecurityRule;
import eu.itesla_project.modules.rules.SecurityRuleSerializer;
import net.sf.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(SecurityRuleSerializer.class)
public class JsonSecurityRuleSerializer implements SecurityRuleSerializer {

    @Override
    public String getFormat() {
        return "json";
    }

    @Override
    public Class<? extends SecurityRule> getSecurityRuleClass() {
        return JsonSecurityRule.class;
    }

    @Override
    public void format(SecurityRule securityRule, OutputStream os) {
        try (Writer writer = new OutputStreamWriter(os)) {
            ((JsonSecurityRule) securityRule).toJSON().write(writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SecurityRule parse(RuleId ruleId, String workflowId, InputStream is) {
        try {
            try {
                String json = new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8);
                return JsonSecurityRule.fromJSON(JSONObject.fromObject(json));
            } finally {
                is.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
