/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.scripting

import com.powsybl.commons.PowsyblException

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class AdderSpec {

    private final def adder

    AdderSpec(Object adder) {
        assert adder
        this.adder = adder
    }

    private def testSetters(String name, args) {
        boolean found = false
        for (String prefix : ["with", "set"]) {
            def setterName = prefix + name.capitalize();
            def setter = adder.metaClass.getMetaMethod(setterName, args)
            if (setter) {
                setter.invoke(adder, args)
                found = true
                break
            }
        }
        return found
    }

    def methodMissing(String name, args) {
        boolean found = testSetters(name, args)
        if (!found && name.startsWith("_")) { // keyword collision strategy
            found = testSetters(name.substring(1), args)
        }
        if (!found) {
            throw new PowsyblException("Setter (method=" + name + ", args=" + args + ") not found")
        }
    }
}
