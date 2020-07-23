/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.scripting

import com.powsybl.commons.PowsyblException
import com.powsybl.commons.extensions.Extension
import com.powsybl.iidm.network.Identifiable

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class IdentifiableExtension {
    static Object propertyMissing(Identifiable self, String name) {
        // first check if an extension exist then a property
        Extension extension = self.getExtensionByName(name)
        if (extension != null) {
            extension
        } else {
            try {
                switch (self.getPropertyType(name)) {
                    case Identifiable.PropertyType.BOOLEAN:
                        self.getBooleanProperty(name)
                        break
                    case Identifiable.PropertyType.DOUBLE:
                        self.getDoubleProperty(name)
                        break
                    case Identifiable.PropertyType.INTEGER:
                        self.getIntegerProperty(name)
                        break
                    default:
                        self.getStringProperty(name)
                }
            } catch (PowsyblException pe) {
                return null
            }
        }
    }

    static void propertyMissing(Identifiable self, String name, Object value) {
        switch(value.getClass().getSimpleName()) {
            case "String":
                self.setStringProperty(name, (String) value)
                break
            case "Double":
                self.setDoubleProperty(name, (double) value)
                break
            case "Integer":
                self.setIntegerProperty(name, (int) value)
                break
            case "Boolean":
                self.setBooleanProperty(name, (boolean) value)
                break
            default:
                self.setStringProperty(name, value)
                break
        }
    }

    /**
     * To fix private field accessibility issue.
     * https://issues.apache.org/jira/browse/GROOVY-3010
     */
    static void setId(Identifiable self, String id) {
        throw new PowsyblException("ID modification of '" + self.id + "' is not allowed")
    }
}
