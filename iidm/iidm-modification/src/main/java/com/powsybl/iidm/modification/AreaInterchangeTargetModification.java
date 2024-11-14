/*
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
import com.powsybl.iidm.network.Area;
import com.powsybl.iidm.network.Network;

import java.util.Objects;

/**
 * Simple {@link NetworkModification} for an area interchange target update.
 *
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class AreaInterchangeTargetModification extends AbstractNetworkModification {

    private final String areaId;

    private final double interchangeTarget;

    public AreaInterchangeTargetModification(String areadId, double interchangeTarget) {
        this.areaId = Objects.requireNonNull(areadId);
        this.interchangeTarget = interchangeTarget;
    }

    @Override
    public String getName() {
        return "AreaInterchangeModification";
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        Area area = network.getArea(areaId);
        if (area == null) {
            logOrThrow(throwException, "Area '" + areaId + "' not found");
            return;
        }
        area.setInterchangeTarget(interchangeTarget);
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        impact = DEFAULT_IMPACT;
        Area area = network.getArea(areaId);
        if (area == null) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        } else {
            if (area.getInterchangeTarget().isPresent() && Math.abs(interchangeTarget - area.getInterchangeTarget().getAsDouble()) < EPSILON
                || area.getInterchangeTarget().isEmpty() && Double.isNaN(interchangeTarget)) {
                impact = NetworkModificationImpact.NO_IMPACT_ON_NETWORK;
            }
        }
        return impact;
    }

    public String getAreaId() {
        return areaId;
    }

    public Double getInterchangeTarget() {
        return interchangeTarget;
    }
}
