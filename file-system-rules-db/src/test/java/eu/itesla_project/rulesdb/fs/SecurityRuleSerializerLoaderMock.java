/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.rulesdb.fs;

import eu.itesla_project.modules.rules.SecurityRule;
import eu.itesla_project.modules.rules.SecurityRuleSerializer;
import eu.itesla_project.modules.rules.SecurityRuleSerializerLoader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityRuleSerializerLoaderMock implements SecurityRuleSerializerLoader {

    private final Map<Class<? extends SecurityRule>, SecurityRuleSerializer> serializers = new HashMap<>();

    public void addSerializer(SecurityRuleSerializer serializer) {
        serializers.put(serializer.getSecurityRuleClass(), serializer);
    }

    @Override
    public SecurityRuleSerializer load(Class<? extends SecurityRule> aClass) {
        return serializers.get(aClass);
    }

    @Override
    public Collection<SecurityRuleSerializer> loadAll() {
        return serializers.values();
    }

}
