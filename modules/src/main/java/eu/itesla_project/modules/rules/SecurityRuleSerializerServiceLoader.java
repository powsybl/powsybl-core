/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.rules;

import eu.itesla_project.commons.util.ServiceLoaderCache;

import java.util.Collection;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityRuleSerializerServiceLoader implements SecurityRuleSerializerLoader {

    private final ServiceLoaderCache<SecurityRuleSerializer> serializers = new ServiceLoaderCache(SecurityRuleSerializer.class);

    @Override
    public SecurityRuleSerializer load(Class<? extends SecurityRule> aClass) {
        for (SecurityRuleSerializer serializer: serializers.getServices()) {
            if (aClass.isAssignableFrom(serializer.getSecurityRuleClass())) {
                return serializer;
            }
        }
        throw new RuntimeException("No serializer found for security rule class " + aClass);
    }

    @Override
    public Collection<SecurityRuleSerializer> loadAll() {
        return serializers.getServices();
    }
}
