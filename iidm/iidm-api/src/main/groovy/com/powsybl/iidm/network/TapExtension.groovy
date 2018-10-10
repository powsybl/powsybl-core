/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network

/**
 * @author Yichen Tang <yichen.tang at rte-france.com>
 */
class TapExtension {

    static Set<String> stepAttributesV10 = ["r", "x", "g", "b"];
    static Set<String> settersV10 = stepAttributesV10.collect {"set" + it.toUpperCase()}

    static Object propertyMissing(TapChangerStep self, String name) {
        println "hi"
        if (stepAttributesV10.contains(name))
            return self.properties["rd" + name]
    }

    static void propertyMissing(TapChangerStep self, String name, Object value) {
        if (stepAttributesV10.contains(name)) {
            println "before set:" + self.properties["rd" + name]
            self.invokeMethod("setRd" + name, value)
            println "after set:" + self.properties["rd" + name]
        } else {
            self.properties[name] = value
        }
    }

    static void setR(TapChangerStep self, double value) {
        self.setRdr(value)
    }

    static double getR(TapChangerStep self) {
        return self.getRdr()
    }

    static void setX(TapChangerStep self, double value) {
        self.setRdx(value)
    }

    static double getX(TapChangerStep self) {
        return self.getRdx()
    }

    static void setG(TapChangerStep self, double value) {
        self.setRdg(value)
    }

    static double getG(TapChangerStep self) {
        return self.getRdg()
    }

    static void setB(TapChangerStep self, double value) {
        self.setRdb(value)
    }

    static double getB(TapChangerStep self) {
        return self.getRdb()
    }

}
