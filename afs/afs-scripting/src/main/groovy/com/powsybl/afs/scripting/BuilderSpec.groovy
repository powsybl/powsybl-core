package com.powsybl.afs.scripting

import com.powsybl.afs.AfsException

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class BuilderSpec {

    private final def builder

    BuilderSpec(Object builder) {
        assert builder
        this.builder = builder
    }

    private def testSetters(String name, args) {
        boolean found = false
        for (String prefix : ["with", "set"]) {
            def setterName = prefix + name.capitalize();
            def setter = builder.metaClass.getMetaMethod(setterName, args)
            if (setter) {
                setter.invoke(builder, args)
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
            throw new AfsException("Setter (method=" + name + ", args=" + args + ") not found")
        }
    }
}
