/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.powsybl.iidm.modification.BoundaryLineModification;
import com.powsybl.iidm.modification.NetworkModification;

import java.util.Objects;

/**
 * An action to:
 * <ul>
 *     <li>change the P0 of a load, either by specifying a new absolute value (MW) or a relative change (MW).</li>
 *     <li>change the Q0 of a load, either by specifying a new absolute value (MVar) or a relative change (MVar).</li>
 * </ul>
 *
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public class BoundaryLineAction extends AbstractLoadAction {

    public static final String NAME = "DANGLING_LINE";

    private final String boundaryLineId;

    /**
     * @param id                 the id of the action.
     * @param boundaryLineId     the id of the boundary line on which the action would be applied.
     * @param relativeValue      True if the boundary line P0 and/or Q0 variation is relative, False if absolute.
     * @param activePowerValue   The new boundary line P0 (MW) if relativeValue equals False, otherwise the relative variation of boundary line P0 (MW).
     * @param reactivePowerValue The new boundary line Q0 (MVar) if relativeValue equals False, otherwise the relative variation of boundary line Q0 (MVar).
     */
    BoundaryLineAction(String id, String boundaryLineId, boolean relativeValue, Double activePowerValue, Double reactivePowerValue) {
        super(id, relativeValue, activePowerValue, reactivePowerValue);
        this.boundaryLineId = Objects.requireNonNull(boundaryLineId);
    }

    public String getBoundaryLineId() {
        return boundaryLineId;
    }

    @Override
    public String getType() {
        return NAME;
    }

    @Override
    public NetworkModification toModification() {
        return new BoundaryLineModification(
                getBoundaryLineId(),
                isRelativeValue(),
                getActivePowerValue().stream().boxed().findFirst().orElse(null),
                getReactivePowerValue().stream().boxed().findFirst().orElse(null)
        );
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
        BoundaryLineAction that = (BoundaryLineAction) o;
        return Objects.equals(boundaryLineId, that.boundaryLineId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), boundaryLineId);
    }
}
