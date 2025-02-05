/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.modification.PercentChangeLoadModification;

import java.util.Objects;

/**
 * An action to:
 * <ul>
 *     <li>change the P0 of a load, by specifying its percentage change (which could be positive or negative).</li>
 *     <li>describe the impact of this change on the Q0 of a load, by specifying the qModificationStrategy.</li>
 * </ul>
 * <p>
 * This action is useful to specify changes that should be applied on a load when its actual active power is unknown.
 * </p>
  *
 * @author Benoît Chiquet {@literal <benoit.chiquet@rte-france.com>}
 */
public class PercentChangeLoadAction extends AbstractAction {

    public static final String NAME = "PCT_LOAD_CHANGE";
    private String loadId;
    private Double p0PercentChange;
    private QModificationStrategy qModificationStrategy;

    /**
     * @param id         the id of the action.
     * @param loadId     the id of the load on which the action would be applied.
     * @param p0PercentChange the percentage that will be added to P0. Negative values describe load reduction.
     * @param qModificationStrategy  the way this change impacts Q0.
     */
    PercentChangeLoadAction(String id, String loadId, Double p0PercentChange, QModificationStrategy qModificationStrategy) {
        super(id);
        this.loadId = loadId;
        this.p0PercentChange = p0PercentChange;
        this.qModificationStrategy = qModificationStrategy;
    }

    public enum QModificationStrategy {
        CONSTANT_Q,
        CONSTANT_PQ_RATIO
    }

    @Override
    public String getType() {
        return NAME;
    }

    public Double getP0PercentChange() {
        return this.p0PercentChange;
    }

    public String getLoadId() {
        return this.loadId;
    }

    public QModificationStrategy getQModificationStrategy() {
        return this.qModificationStrategy;
    }

    @Override
    public NetworkModification toModification() {
        double q0PercentChange = switch (qModificationStrategy) {
            case CONSTANT_Q -> 0d;
            case CONSTANT_PQ_RATIO -> p0PercentChange;
        };
        return new PercentChangeLoadModification(loadId, p0PercentChange, q0PercentChange, true);
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
        PercentChangeLoadAction that = (PercentChangeLoadAction) o;
        return Objects.equals(loadId, that.loadId) && Objects.equals(p0PercentChange, that.p0PercentChange) && qModificationStrategy == that.qModificationStrategy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), loadId, p0PercentChange, qModificationStrategy);
    }
}
