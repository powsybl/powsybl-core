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
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;

import java.util.Objects;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class ThreeWindingsTransformerModification extends AbstractNetworkModification {

    private final String transformerId;
    private final Double ratedU0;

    public ThreeWindingsTransformerModification(String transformerId, double ratedU0) {
        this.transformerId = Objects.requireNonNull(transformerId);
        this.ratedU0 = ratedU0;
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException,
                      ComputationManager computationManager, ReportNode reportNode) {
        ThreeWindingsTransformer t3wt = network.getThreeWindingsTransformer(transformerId);
        if (t3wt == null) {
            logOrThrow(throwException, "ThreeWindingsTransformer '" + transformerId + "' not found");
            return;
        }
        if (ratedU0 > 0) {
            t3wt.getLeg1().setRatedU(calculateNewRatedU(t3wt.getLeg1().getRatedU(), t3wt.getRatedU0(), ratedU0));
            t3wt.getLeg2().setRatedU(calculateNewRatedU(t3wt.getLeg2().getRatedU(), t3wt.getRatedU0(), ratedU0));
            t3wt.getLeg3().setRatedU(calculateNewRatedU(t3wt.getLeg3().getRatedU(), t3wt.getRatedU0(), ratedU0));
            t3wt.setRatedU0(ratedU0);
        }
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        ThreeWindingsTransformer t3wt = network.getThreeWindingsTransformer(transformerId);
        if (t3wt == null) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        } else if (ratedU0 > 0 && Math.abs(ratedU0 - t3wt.getRatedU0()) > EPSILON) {
            impact = NetworkModificationImpact.HAS_IMPACT_ON_NETWORK;
        }
        return impact;
    }

    private static double calculateNewRatedU(double ratedU, double ratedU0, double newRatedU0) {
        return ratedU * newRatedU0 / ratedU0;
    }

    public String getTransformerId() {
        return transformerId;
    }

    public Double getRatedU0() {
        return ratedU0;
    }
}
