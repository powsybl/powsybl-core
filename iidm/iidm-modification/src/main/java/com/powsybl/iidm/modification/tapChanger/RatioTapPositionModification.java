/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.tapChanger;

import com.powsybl.iidm.network.*;

/**
 * @author Nicolas PIERRE <nicolas.pierre at artelys.com>
 */
public class RatioTapPositionModification extends AbstractTapPositionModification {

    /**
     * @param transfoId   the ID of the two windings transformer, which holds the rtc
     * @param tapPosition the new tap position
     */
    public static RatioTapPositionModification createTwoWindingsRtcPositionModification(String transfoId,
                                                                                        int tapPosition) {
        return new RatioTapPositionModification(transfoId, tapPosition, null);
    }

    /**
     * @param transfoId   the ID of the three windings transformer, which holds the rtc
     * @param tapPosition the new tap position
     * @param legNumber   Defines the leg on which to apply the change for three winding tranformers. 0, 1 or 2
     */
    public static RatioTapPositionModification createThreeWindingsRtcPositionModification(String transfoId,
                                                                                          int tapPosition,
                                                                                          int legNumber) {
        return new RatioTapPositionModification(transfoId, tapPosition, legNumber);
    }

    /**
     * @param transfoId   the ID of the three windings transformer, which holds the rtc
     * @param tapPosition the new tap position
     * @param leg         defines on which leg of the three winding transformer the modification will be done. null on two windings.
     * @see RatioTapPositionModification#createThreeWindingsRtcPositionModification(String, int, int)
     * @see RatioTapPositionModification#createTwoWindingsRtcPositionModification(String, int)
     */
    public RatioTapPositionModification(String transfoId, int tapPosition, Integer leg) {
        super(transfoId, tapPosition, leg);
    }

    @Override
    protected void applyTwoWindings(Network network, TwoWindingsTransformer twoWindingsTransformer,
                                    boolean throwException) {
        apply(twoWindingsTransformer, throwException);
    }

    @Override
    protected void applyThreeWindings(Network network, ThreeWindingsTransformer threeWindingsTransformer,
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

}
