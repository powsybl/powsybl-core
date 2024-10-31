/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;

import java.util.Objects;

/**
 * {@link NetworkModification} changing the active and reactive powers of a load by defining percentage changes (which could be positive or negative).
 *
 * @author Benoît Chiquet {@literal <benoit.chiquet at rte-france.com>}
 */
public class PercentChangeLoadModification extends AbstractNetworkModification {

    private String loadId;
    private double q0PercentChange;
    private double p0PercentChange;

    public PercentChangeLoadModification(String loadId, double p0PercentChange, double q0PercentChange) {
        this.loadId = Objects.requireNonNull(loadId);
        if (p0PercentChange < -100) {
            throw new PowsyblException("The active power of " + loadId + " cannot decrease by more than 100% (current value: " + p0PercentChange + ")");
        }
        if (q0PercentChange < -100) {
            throw new PowsyblException("The reactive power of " + loadId + " cannot decrease by more than 100% (current value: " + q0PercentChange + ")");
        }
        this.p0PercentChange = p0PercentChange;
        this.q0PercentChange = q0PercentChange;
    }

    @Override
    public String getName() {
        return "pctLoadModification";
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        Load load = network.getLoad(loadId);
        if (load == null) {
            logOrThrow(throwException, "Load '" + loadId + "' not found");
        } else {
            double p0 = load.getP0();
            load.setP0(p0 + (p0 * p0PercentChange / 100));
            double q0 = load.getQ0();
            load.setQ0(q0 + (q0 * q0PercentChange / 100));
        }
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        impact = DEFAULT_IMPACT;
        Load load = network.getLoad(loadId);
        if (load == null) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        } else if (p0PercentChange == 0 && q0PercentChange == 0) {
            impact = NetworkModificationImpact.NO_IMPACT_ON_NETWORK;
        }
        return impact;
    }

    public String getMessageHeader() {
        return getName();
    }
}
