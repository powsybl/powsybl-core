/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class IdentifiableExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentifiableExtension.class);

    static Object propertyMissing(Identifiable self, String name) {
        // first check if an extension exist then a property
        Identifiable.Extension extension = self.getExtensionByName(name)
        extension != null ? extension : self.properties[name]
    }

    static void propertyMissing(Identifiable self, String name, Object value) {
        if (!self.hasProperty(name))
            LOGGER.warn("Network component '{}' has not {} property", self, name)
        self.properties[name] = value
    }
}