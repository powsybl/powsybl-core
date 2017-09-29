/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl.task;

import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.tasks.ModificationTask;
import com.powsybl.iidm.network.Network;
import groovy.lang.Closure;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ScriptTask implements ModificationTask {

    private final Closure script;

    public ScriptTask(Closure<Void> script) {
        this.script = Objects.requireNonNull(script);
    }

    public Closure<Void> getScript() {
        return script;
    }

    @Override
    public void modify(Network network, ComputationManager computationManager) {
        script.call(network, computationManager);
    }
}
