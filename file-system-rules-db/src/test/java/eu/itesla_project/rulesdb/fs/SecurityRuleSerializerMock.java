/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.rulesdb.fs;

import eu.itesla_project.modules.rules.RuleId;
import eu.itesla_project.modules.rules.SecurityRule;
import eu.itesla_project.modules.rules.SecurityRuleSerializer;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityRuleSerializerMock implements SecurityRuleSerializer {
    @Override
    public void format(SecurityRule securityRule, OutputStream os) {
    }

    @Override
    public String getFormat() {
        return "tst";
    }

    @Override
    public Class<? extends SecurityRule> getSecurityRuleClass() {
        return SecurityRuleMock.class;
    }

    @Override
    public SecurityRule parse(RuleId ruleId, String workflowId, InputStream is) {
        return new SecurityRuleMock(ruleId, workflowId);
    }
}
