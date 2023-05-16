/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.tap;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerHolder;

/**
 * @author Nicolas PIERRE <nicolas.pierre at artelys.com>
 */
public class RatioTapPositionModification extends AbstractTapPositionModification {

    /**
     * @param transfoId   the ID of the two windings transformer, which holds the rtc
     * @param tapPosition the new tap position
     */
    public static RatioTapPositionModification createTwoWindingsRtcPosition(String transfoId, int tapPosition) {
        return new RatioTapPositionModification(transfoId,
            TransformerType.TWO_WINDINGS_TRANSFORMER, tapPosition, null);
    }

    /**
     * @param transfoId   the ID of the three windings transformer, which holds the rtc
     * @param tapPosition the new tap position
     * @param legNumber   Defines the leg on which to apply the change for three winding tranformers. 0, 1 or 2
     */
    public static RatioTapPositionModification createThreeWindingsRtcPosition(String transfoId, int tapPosition,
                                                                              int legNumber) {
        return new RatioTapPositionModification(transfoId,
            TransformerType.THREE_WINDINGS_TRANSFORMER, tapPosition, legNumber);
    }

    /**
     * @param transfoId   the ID of the three windings transformer, which holds the rtc
     * @param tapPosition the new tap position
     * @param leg         defines on which leg of the three winding transformer the modification will be done. null on two windings.
     */
    public RatioTapPositionModification(String transfoId, TransformerType element,
                                        int tapPosition, Integer leg) {
        super(transfoId, element, tapPosition, leg);
    }

    @Override
    public void apply(Network network, boolean throwException) {
        RatioTapChangerHolder transformer = null;
        if (getElement() == TransformerType.TWO_WINDINGS_TRANSFORMER) {
            transformer = network.getTwoWindingsTransformer(getTransfoId());
        } else if (getElement() == TransformerType.THREE_WINDINGS_TRANSFORMER) {
            transformer = getLeg(network.getThreeWindingsTransformer(getTransfoId()));
        }

        if (transformer == null) {
            logOrThrow(throwException, TRANSFORMER_STR + getTransfoId() + "' not found");
            return;
        }
        if (!transformer.hasRatioTapChanger()) {
            logOrThrow(throwException, TRANSFORMER_STR + getTransfoId() + "' does not have a RatioTapChanger");
            return;
        }
        RatioTapChanger rtc = transformer.getRatioTapChanger();
        if (getTapPosition() < rtc.getLowTapPosition() || getTapPosition() > rtc.getHighTapPosition()) {
            logOrThrow(throwException,
                "RatioTapChanger of transformer '" + getTransfoId() + "' can't be set to the value given (out of Tap range).");
            return;
        }
        transformer.getRatioTapChanger().setTapPosition(getTapPosition());
    }
}
