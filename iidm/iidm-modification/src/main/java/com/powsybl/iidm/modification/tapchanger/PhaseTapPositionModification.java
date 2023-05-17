/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.tapchanger;

import com.powsybl.iidm.network.*;

/**
 * @author Nicolas PIERRE <nicolas.pierre at artelys.com>
 */
public class PhaseTapPositionModification extends AbstractTapPositionModification {

    /**
     * @param transfoId   the ID of the two windings transformer, which holds the ptc
     * @param tapPosition the new tap position
     */
    public static PhaseTapPositionModification createTwoWindingsPtcPositionModification(String transfoId,
                                                                                        int tapPosition) {
        return new PhaseTapPositionModification(transfoId, tapPosition, null);
    }

    /**
     * @param transfoId   the ID of the three windings transformer, which holds the ptc
     * @param tapPosition the new tap position
     * @param leg         Defines the leg on which to apply the change for three winding tranformers.
     */
    public static PhaseTapPositionModification createThreeWindingsPtcPositionModification(String transfoId,
                                                                                          int tapPosition,
                                                                                          ThreeWindingsTransformer.Side leg) {
        return new PhaseTapPositionModification(transfoId, tapPosition, leg);
    }

    /**
     * @param transfoId   the ID of the three windings transformer, which holds the ptc
     * @param tapPosition the new tap position
     * @param leg         defines on which leg of the three winding transformer the modification will be done. null on two windings.
     * @see PhaseTapPositionModification#createThreeWindingsPtcPositionModification(String, int, ThreeWindingsTransformer.Side)
     * @see PhaseTapPositionModification#createTwoWindingsPtcPositionModification(String, int)
     */
    public PhaseTapPositionModification(String transfoId, int tapPosition, ThreeWindingsTransformer.Side leg) {
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
