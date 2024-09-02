/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.ial.dsl.modification;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.NetworkModificationImpact;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.Network;
import groovy.lang.Closure;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
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
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException,
                      ComputationManager computationManager, ReportNode reportNode) {
        script.call(network, computationManager);
    }
}
