/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.scripting

import com.powsybl.commons.PowsyblException
import com.powsybl.commons.extensions.Extension
import com.powsybl.commons.extensions.ExtensionAdderProviders
import com.powsybl.iidm.network.Identifiable

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class IdentifiableExtension {

    static Object propertyMissing(Identifiable self, String name) {
        // first check if an extension exist then a property
        Extension extension = self.getExtensionByName(name)
        extension != null ? extension : self.properties[name]
    }

    static void propertyMissing(Identifiable self, String name, Object value) {
        if (value == null) {
            self.properties.remove(name)
        } else {
            self.properties[name] = value;
        }
    }

    /**
     * To fix private field accessibility issue.
     * https://issues.apache.org/jira/browse/GROOVY-3010
     */
    static void setId(Identifiable self, String id) {
        throw new PowsyblException("ID modification of '" + self.id + "' is not allowed")
    }

    private static String findExtensionName(String name, args) {
        if (name.endsWith("Adder") && name.size() > 5 && args.length == 0) {
            name.substring(0, name.length() - 5)
        } else if (name.startsWith("new") && name.size() > 3
                && (args.length == 0 || (args.length == 1 && args[0] instanceof Closure))) {
            name.substring(3, name.length()).substring(0).uncapitalize()
        }
    }

    private static createExtensionAdder(extensionName, delegate) {
        def extensionAdderProvider = ExtensionAdderProviders.findCachedProvider(delegate.getImplementationName(), extensionName)
        extensionAdderProvider.newAdder(delegate)
    }

    static Object methodMissing(Identifiable self, String name, Object args) {
        String extensionName;
        if ((extensionName = findExtensionName(name, args))) {
            if (args.length == 0) {
                createExtensionAdder(extensionName, self)
            } else {
                def closure = args[0]

                def adder = createExtensionAdder(extensionName, self)

                def cloned = closure.clone()
                AdderSpec spec = new AdderSpec(adder)
                cloned.delegate = spec
                cloned()

                adder.add();
            }
        }
    }
}
