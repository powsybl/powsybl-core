/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl.modification;

import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.network.Network;
import groovy.lang.Closure;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ScriptNetworkModification extends AbstractNetworkModification {

    private final Closure<Void> script;

    public ScriptNetworkModification(Closure<Void> script) {
        this.script = Objects.requireNonNull(script);
    }

    public Closure<Void> getScript() {
        return script;
    }

    @Override
    public void apply(Network network, boolean throwException,
                      ComputationManager computationManager, Reporter reporter) {
        script.call(network, computationManager);
    }
}
