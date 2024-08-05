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
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Network;

import java.util.Objects;

/**
 * Simple {@link NetworkModification} for a dangling line.
 *
 * @author Pauline JEAN-MARIE {@literal <pauline.jean-marie at artelys.com>}
 */
public class DanglingLineModification extends AbstractLoadModification {

    private final String danglingLineId;

    /**
     * @param danglingLineId    the id of the dangling line on which the action would be applied.
     * @param relativeValue True if the dangling line P0 and/or Q0 variation is relative, False if absolute.
     * @param p0                The new dangling line P0 (MW) if relativeValue equals False, otherwise the relative variation of dangling line P0 (MW).
     * @param q0                The new dangling line Q0 (MVar) if relativeValue equals False, otherwise the relative variation of dangling line Q0 (MVar).
     */
    public DanglingLineModification(String danglingLineId, boolean relativeValue, Double p0, Double q0) {
        super(p0, q0, relativeValue);
        this.danglingLineId = Objects.requireNonNull(danglingLineId);
    }

    public DanglingLineModification(String danglingLineId, Double targetP0, Double targetQ0) {
        this(danglingLineId, false, targetP0, targetQ0);
    }

    public String getDanglingLineId() {
        return danglingLineId;
    }

    @Override
    public void doApply(Network network, NamingStrategy namingStrategy, boolean throwException,
                        ComputationManager computationManager, ReportNode reportNode, boolean dryRun) {
        DanglingLine danglingLine = network.getDanglingLine(getDanglingLineId());
        if (danglingLine == null) {
            logOrThrow(throwException, "DanglingLine '" + getDanglingLineId() + "' not found");
            return;
        }
        getP0().ifPresent(value -> danglingLine.setP0((isRelativeValue() ? danglingLine.getP0() : 0) + value, dryRun));
        getQ0().ifPresent(value -> danglingLine.setQ0((isRelativeValue() ? danglingLine.getQ0() : 0) + value, dryRun));
    }

    @Override
    public String getName() {
        return "DanglingLineModification";
    }

    @Override
    public boolean hasImpactOnNetwork() {
        return false;
    }

    @Override
    public boolean isLocalDryRunPossible() {
        return true;
    }
}
