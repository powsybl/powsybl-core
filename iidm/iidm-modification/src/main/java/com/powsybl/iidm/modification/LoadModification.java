/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;

import java.util.Objects;

/**
 * Simple {@link NetworkModification} for a load.
 *
 * @author Pauline JEAN-MARIE {@literal <pauline.jean-marie at artelys.com>}
 */
public class LoadModification extends AbstractLoadModification {

    private final String loadId;

    /**
     * @param loadId    the id of the load on which the action would be applied.
     * @param relativeValue True if the load P0 and/or Q0 variation is relative, False if absolute.
     * @param p0                The new load P0 (MW) if relativeValue equals False, otherwise the relative variation of load P0 (MW).
     * @param q0                The new load Q0 (MVar) if relativeValue equals False, otherwise the relative variation of load Q0 (MVar).
     */
    public LoadModification(String loadId, boolean relativeValue, Double p0, Double q0) {
        super(p0, q0, relativeValue);
        this.loadId = Objects.requireNonNull(loadId);
    }

    public LoadModification(String loadId, Double targetP0, Double targetQ0) {
        this(loadId, false, targetP0, targetQ0);
    }

    @Override
    public String getName() {
        return "LoadModification";
    }

    public String getLoadId() {
        return loadId;
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager,
                      ReportNode reportNode) {
        Load load = network.getLoad(getLoadId());
        if (load == null) {
            logOrThrow(throwException, "Load '" + getLoadId() + "' not found");
            return;
        }
        getP0().ifPresent(value -> load.setP0((isRelativeValue() ? load.getP0() : 0) + value));
        getQ0().ifPresent(value -> load.setQ0((isRelativeValue() ? load.getQ0() : 0) + value));
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        impact = DEFAULT_IMPACT;
        Load load = network.getLoad(getLoadId());
        if (load == null) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        } else if (areValuesEqual(p0, load.getP0(), isRelativeValue()) && areValuesEqual(q0, load.getQ0(), isRelativeValue())) {
            impact = NetworkModificationImpact.NO_IMPACT_ON_NETWORK;
        }
        return impact;
    }
}
