/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tapchanger;

import com.powsybl.iidm.modification.NetworkModificationImpact;
import com.powsybl.iidm.network.*;

import java.util.Objects;

import static com.powsybl.iidm.modification.util.ModificationLogs.logOrThrow;

/**
 * @author Nicolas PIERRE {@literal <nicolas.pierre at artelys.com>}
 */
public class PhaseTapPositionModification extends AbstractTapPositionModification {

    private boolean isRelative = false;

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

    /**
     * Creates a PTC tap modification for two windings transformers, or three windings transformer with a single PTC.
     *
     * @param transformerId the ID of the transformer, which holds the PTC
     * @param tapPosition   the new tap position
     * @param isRelative    is the new tap position relative to the previous one or absolute
     */
    public PhaseTapPositionModification(String transformerId, int tapPosition, boolean isRelative) {
        super(transformerId, tapPosition, null);
        this.isRelative = isRelative;
    }

    /**
     * Creates a PTC tap modification for three windings transformers on the given leg.
     *
     * @param transformerId the ID of the three windings transformer, which holds the PTC
     * @param tapPosition   the new tap position
     * @param leg           defines on which leg of the three winding transformer the modification will be done.
     * @param isRelative    is the new tap position relative to the previous one or absolute
     */
    public PhaseTapPositionModification(String transformerId, int tapPosition, ThreeSides leg, boolean isRelative) {
        super(transformerId, tapPosition, Objects.requireNonNull(leg));
        this.isRelative = isRelative;
    }

    @Override
    public String getName() {
        return "PhaseTapPositionModification";
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
            int newTapPosition = (isRelative ? ptcHolder.getPhaseTapChanger().getTapPosition() : 0) + getTapPosition();
            ptcHolder.getPhaseTapChanger().setTapPosition(newTapPosition);
        } catch (ValidationException e) {
            logOrThrow(throwException, e.getMessage());
            return;
        }
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        impact = DEFAULT_IMPACT;
        TwoWindingsTransformer twoWindingsTransformer = network.getTwoWindingsTransformer(getTransformerId());
        ThreeWindingsTransformer threeWindingsTransformer = network.getThreeWindingsTransformer(getTransformerId());
        if (twoWindingsTransformer == null && threeWindingsTransformer == null) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        } else if (twoWindingsTransformer != null) {
            if (cannotApplyModification(twoWindingsTransformer)) {
                impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
            } else if (areValuesEqual(getTapPosition(), twoWindingsTransformer.getPhaseTapChanger().getTapPosition(), isRelative)) {
                impact = NetworkModificationImpact.NO_IMPACT_ON_NETWORK;
            }
        } else {
            PhaseTapChangerHolder ptcHolder = getLeg(threeWindingsTransformer, PhaseTapChangerHolder::hasPhaseTapChanger, false);
            if (cannotApplyModification(ptcHolder)) {
                impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
            } else if (areValuesEqual(getTapPosition(), ptcHolder.getPhaseTapChanger().getTapPosition(), isRelative)) {
                impact = NetworkModificationImpact.NO_IMPACT_ON_NETWORK;
            }
        }
        return impact;
    }

    private boolean cannotApplyModification(TwoWindingsTransformer twoWindingsTransformer) {
        return !twoWindingsTransformer.hasPhaseTapChanger()
            || isValueOutsideRange(getTapPosition() + (isRelative ? twoWindingsTransformer.getPhaseTapChanger().getTapPosition() : 0),
            twoWindingsTransformer.getPhaseTapChanger().getLowTapPosition(),
            twoWindingsTransformer.getPhaseTapChanger().getHighTapPosition());
    }

    private boolean cannotApplyModification(PhaseTapChangerHolder ptcHolder) {
        return !ptcHolder.hasPhaseTapChanger()
            || isValueOutsideRange(getTapPosition() + (isRelative ? ptcHolder.getPhaseTapChanger().getTapPosition() : 0),
            ptcHolder.getPhaseTapChanger().getLowTapPosition(),
            ptcHolder.getPhaseTapChanger().getHighTapPosition());
    }
}
