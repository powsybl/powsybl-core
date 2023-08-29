/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.util.VoltageRegulationUtils;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;

import java.util.Objects;

/**
 * @author Olivier Perrin <olivier.perrin at rte-france.com>
 */
public final class ConnectGenerator extends AbstractNetworkModification {

    private final String generatorId;

    public ConnectGenerator(String generatorId) {
        this.generatorId = generatorId;
    }

    @Override
    public void apply(Network network, boolean throwException,
                      ComputationManager computationManager, Reporter reporter) {
        Generator g = network.getGenerator(generatorId);
        if (g == null) {
            throw new PowsyblException("Generator '" + generatorId + "' not found");
        }

        connect(g);
    }

    static void connect(Generator g) {
        Objects.requireNonNull(g);

        if (g.getTerminal().isConnected()) {
            return;
        }

        Terminal t = g.getTerminal();
        t.connect();
        if (g.isVoltageRegulatorOn()) {
            VoltageRegulationUtils.getTargetVForRegulatingElement(g.getNetwork(), g.getRegulatingTerminal().getBusView().getBus(), g.getId(), IdentifiableType.GENERATOR)
                    .ifPresent(g::setTargetV);
        }
    }
}
