/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class GroovyUtil {

    static Object callProperty(Object obj, String name) {
        obj."$name"
    }

    static Object callMethod(Object obj, String name, Object[] args) {
        obj.invokeMethod(name, args)
    }

}
