/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.modification.PctLoadModification;

import java.util.Objects;

/**
 * An action to:
 * <ul>
 *     <li>change the P0 of a load, by specifying its percentage change (which could be positive or negative).</li>
 *     <li>describe the impact of this change on the Q0 of a load, by specifying the qStrategy.</li>
 * </ul>
 * <p>
 * This action is useful to specify changes that should be applied on a load when its actual active power is unknown.
 * </p>
  *
 * @author Benoît Chiquet {@literal <benoit.chiquet@rte-france.com>}
 */
public class PctLoadAction extends AbstractAction {

    public static final String NAME = "PCT_LOAD_CHANGE";
    private String loadId;
    private Double pctPChange;
    private QModificationStrategy qStrategy;

    /**
     * @param id         the id of the action.
     * @param loadId     the id of the load on which the action would be applied.
     * @param pctPChange the percentage that will be added to P0. Negative values describe load reduction.
     * @param qStrategy  the way this change impacts Q0.
     */
    PctLoadAction(String id, String loadId, Double pctPChange, QModificationStrategy qStrategy) {
        super(id);
        this.loadId = loadId;
        this.pctPChange = pctPChange;
        this.qStrategy = qStrategy;
    }

    public enum QModificationStrategy {
        CONSTANT_Q,
        CONSTANT_PQ_RATIO
    }

    @Override
    public String getType() {
        return NAME;
    }

    public Double getPctPChange() {
        return this.pctPChange;
    }

    public String getLoadId() {
        return this.loadId;
    }

    public QModificationStrategy getQStrategy() {
        return this.qStrategy;
    }

    @Override
    public NetworkModification toModification() {
        double pctQChange = switch (qStrategy) {
            case CONSTANT_Q -> 0d;
            case CONSTANT_PQ_RATIO -> pctPChange;
        };
        return new PctLoadModification(loadId, pctPChange, pctQChange);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        PctLoadAction that = (PctLoadAction) o;
        return Objects.equals(loadId, that.loadId) && Objects.equals(pctPChange, that.pctPChange) && qStrategy == that.qStrategy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), loadId, pctPChange, qStrategy);
    }
}
