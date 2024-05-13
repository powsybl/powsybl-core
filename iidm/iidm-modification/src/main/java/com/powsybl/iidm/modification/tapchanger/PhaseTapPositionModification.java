/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tapchanger;

import com.powsybl.iidm.network.*;

import java.util.Objects;

/**
 * @author Nicolas PIERRE {@literal <nicolas.pierre at artelys.com>}
 */
public class PhaseTapPositionModification extends AbstractTapPositionModification {

    /**
     * Creates a PTC tap modification for two windings transformers, or three windings transformer with a single PTC.
     *
     * @param transformerId the ID of the transformer, which holds the PTC
     * @param tapPosition   the new tap position
     */
    public PhaseTapPositionModification(String transformerId, int tapPosition) {
        super(transformerId, tapPosition, null);
    }

    /**
     * Creates a PTC tap modification for three windings transformers on the given leg.
     *
     * @param transformerId the ID of the three windings transformer, which holds the PTC
     * @param tapPosition   the new tap position
     * @param leg           defines on which leg of the three winding transformer the modification will be done.
     */
    public PhaseTapPositionModification(String transformerId, int tapPosition, ThreeSides leg) {
        super(transformerId, tapPosition, Objects.requireNonNull(leg));
    }

    @Override
    protected void applyTwoWindingsTransformer(Network network, TwoWindingsTransformer twoWindingsTransformer,
                                               boolean throwException) {
        apply(twoWindingsTransformer, throwException);
    }

    @Override
    protected void applyThreeWindingsTransformer(Network network, ThreeWindingsTransformer threeWindingsTransformer,
                                                 boolean throwException) {
        apply(getLeg(threeWindingsTransformer, PhaseTapChangerHolder::hasPhaseTapChanger, throwException),
            throwException);
    }

    public void apply(PhaseTapChangerHolder ptcHolder, boolean throwException) {
        if (ptcHolder == null) {
            logOrThrow(throwException, "Failed to apply : " + TRANSFORMER_STR + getTransformerId());
            return;
        }
        if (!ptcHolder.hasPhaseTapChanger()) {
            logOrThrow(throwException, TRANSFORMER_STR + getTransformerId() + "' does not have a PhaseTapChanger");
            return;
        }
        try {
            ptcHolder.getPhaseTapChanger().setTapPosition(getTapPosition());
        } catch (ValidationException e) {
            logOrThrow(throwException, e.getMessage());
        }
    }
}
