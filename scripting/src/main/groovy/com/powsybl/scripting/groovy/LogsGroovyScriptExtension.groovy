package com.powsybl.scripting.groovy

import com.google.auto.service.AutoService

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
@AutoService(GroovyScriptExtension.class)
class LogsGroovyScriptExtension implements GroovyScriptExtension {

    LogsGroovyScriptExtension() {}

    @Override
    void load(Binding binding, Map<Class<?>, Object> contextObjects) {
        Writer writer = contextObjects.get(Writer.class) as Writer
        if (writer != null) {
            binding.out = writer
        }
    }

    @Override
    void unload() {}
}
