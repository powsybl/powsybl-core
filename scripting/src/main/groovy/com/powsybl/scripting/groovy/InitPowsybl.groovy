/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.scripting.groovy

import com.powsybl.computation.ComputationManager
import com.powsybl.computation.DefaultComputationManagerConfig
import org.codehaus.groovy.tools.shell.CommandSupport
import org.codehaus.groovy.tools.shell.Groovysh

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class InitPowsybl extends CommandSupport {

    protected InitPowsybl(final Groovysh shell) {
        super(shell, ':init_powsybl', ':powsybl')
    }

    Object execute(List args) {
        // Computation manager
        DefaultComputationManagerConfig config = DefaultComputationManagerConfig.load();
        ComputationManager computationManager = config.createShortTimeExecutionComputationManager();

        // load extensions
        final Iterable<GroovyScriptExtension> extensions = ServiceLoader.load(GroovyScriptExtension.class)
        extensions.forEach { it.load(getBinding(), computationManager) }

        // Unload extensions at exit
        Runtime.getRuntime().addShutdownHook(new Thread({
            extensions.forEach { it.unload() }
        }));
    }
}
