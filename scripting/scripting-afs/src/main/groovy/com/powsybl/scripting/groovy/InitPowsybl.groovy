/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.scripting.groovy

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.powsybl.afs.AppData
import com.powsybl.computation.DefaultComputationManagerConfig
import org.codehaus.groovy.tools.shell.CommandSupport
import org.codehaus.groovy.tools.shell.Groovysh
import org.slf4j.LoggerFactory

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class InitPowsybl extends CommandSupport {

    protected InitPowsybl(final Groovysh shell) {
        super(shell, ':init_powsybl', ':powsybl')
    }

    Object execute(List args) {
        // force logback to error level
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.ERROR);

        DefaultComputationManagerConfig config = DefaultComputationManagerConfig.load();
        getVariables().put("afs", new AfsGroovyFacade(new AppData(config.createShortTimeExecutionComputationManager(),
                                                                  config.createLongTimeExecutionComputationManager())))
    }
}
