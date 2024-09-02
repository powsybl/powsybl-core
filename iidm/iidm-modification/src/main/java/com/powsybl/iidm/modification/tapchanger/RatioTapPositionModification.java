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

/**
 * @author Nicolas PIERRE {@literal <nicolas.pierre at artelys.com>}
 */
public class RatioTapPositionModification extends AbstractTapPositionModification {

    /**
     * Creates a RTC tap modification for two windings transformers, or three windings transformer with a single RTC.
     *
     * @param transformerId the ID of the transformer, which holds the rtc
     * @param tapPosition   the new tap position
     */
    public RatioTapPositionModification(String transformerId, int tapPosition) {
        super(transformerId, tapPosition, null);
    }

    /**
     * Creates a RTC tap modification for three windings transformers on the given leg.
     *
     * @param transformerId the ID of the three windings transformer, which holds the rtc
     * @param tapPosition   the new tap position
     * @param leg           defines on which leg of the three winding transformer the modification will be done.
     */
    public RatioTapPositionModification(String transformerId, int tapPosition, ThreeSides leg) {
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
        apply(getLeg(threeWindingsTransformer, RatioTapChangerHolder::hasRatioTapChanger, throwException),
            throwException);
    }

    public void apply(RatioTapChangerHolder rtcHolder, boolean throwException) {
        if (rtcHolder == null) {
            logOrThrow(throwException, "Failed to apply : " + TRANSFORMER_STR + getTransformerId());
            return;
        }
        if (!rtcHolder.hasRatioTapChanger()) {
            logOrThrow(throwException, TRANSFORMER_STR + getTransformerId() + "' does not have a RatioTapChanger");
            return;
        }
        try {
            rtcHolder.getRatioTapChanger().setTapPosition(getTapPosition());
        } catch (ValidationException e) {
            logOrThrow(throwException, e.getMessage());
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
            if (!twoWindingsTransformer.hasPhaseTapChanger()) {
                impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
            } else if (Math.abs(getTapPosition() - twoWindingsTransformer.getPhaseTapChanger().getTapPosition()) < EPSILON) {
                impact = NetworkModificationImpact.NO_IMPACT_ON_NETWORK;
            }
        } else {
            PhaseTapChangerHolder ptcHolder = getLeg(threeWindingsTransformer, PhaseTapChangerHolder::hasPhaseTapChanger, false);
            if (!ptcHolder.hasPhaseTapChanger()) {
                impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
            } else if (Math.abs(getTapPosition() - ptcHolder.getPhaseTapChanger().getTapPosition()) < EPSILON) {
                impact = NetworkModificationImpact.NO_IMPACT_ON_NETWORK;
            }
        }
        return impact;
    }
}
