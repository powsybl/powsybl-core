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
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationException;

import java.util.Objects;

/**
 * {@link NetworkModification} for a load where load change depends on existing load.
 *
 * @author Benoît Chiquet {@literal <benoit.chiquet at rte-france.com>}
 */
public class PctLoadModification extends AbstractNetworkModification implements Validable {

    private String loadId;
    private double pctQChange;
    private double p0PercentChange;

    public PctLoadModification(String loadId, double p0PercentChange, double pctQChange) {
        this.loadId = Objects.requireNonNull(loadId);
        if (p0PercentChange < -100 || pctQChange < -100) {
            throw new ValidationException(this, ": Can't decrease load by more than 100% on " + loadId);
        }
        this.p0PercentChange = p0PercentChange;
        this.pctQChange = pctQChange;
    }

    @Override
    public String getName() {
        return "pctLoadModification";
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        Load load = network.getLoad(loadId);
        if (load == null) {
            throw new PowsyblException(getMessageHeader() + ": Tried to apply modification on " + loadId + " but no load was found");
        }
        double p0 = load.getP0();
        load.setP0(p0 + (p0 * p0PercentChange / 100));
        double q0 = load.getQ0();
        load.setQ0(q0 + (q0 * pctQChange / 100));
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        impact = DEFAULT_IMPACT;
        Load load = network.getLoad(loadId);
        if (load == null) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        } else if (p0PercentChange == 0 && pctQChange == 0) {
            impact = NetworkModificationImpact.NO_IMPACT_ON_NETWORK;
        }
        return impact;
    }

    @Override
    public String getMessageHeader() {
        return getName();
    }
}
