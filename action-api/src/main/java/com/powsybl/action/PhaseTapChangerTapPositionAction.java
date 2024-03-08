/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action;

import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.modification.tapchanger.PhaseTapPositionModification;
import com.powsybl.iidm.network.ThreeSides;

/**
 * An action changing the tap position of a phase-shifting transformer.
 *
 * @author Hadrien Godard {@literal <hadrien.godard@artelys.com>}
 */
public class PhaseTapChangerTapPositionAction extends AbstractTapChangerTapPositionAction {

    public static final String NAME = "PHASE_TAP_CHANGER_TAP_POSITION";

    public PhaseTapChangerTapPositionAction(String id, String transformerId, boolean relativeValue, int tapPosition) {
        super(id, transformerId, relativeValue, tapPosition, null);
    }

    public PhaseTapChangerTapPositionAction(String id, String transformerId, boolean relativeValue, int tapPosition, ThreeSides side) {
        super(id, transformerId, relativeValue, tapPosition, side);
    }

    @Override
    public String getType() {
        return NAME;
    }

    public NetworkModification toModification() {
        // did not use PhaseShifterShiftTap in case of relative change because PhaseShifterShiftTap change also regulating and its mode
        // so update PhaseTapPositionModification with is relative instead
        // in fact side should always be filled for 3wt and is never used for 2wt..., action and modification should be separated for this 2 cases?
        return getSide().map(side -> new PhaseTapPositionModification(getTransformerId(), getTapPosition(), side, isRelativeValue()))
                        .orElse(new PhaseTapPositionModification(getTransformerId(), getTapPosition(), isRelativeValue()));
    }
}
