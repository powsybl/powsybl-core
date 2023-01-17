/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.action;

import com.powsybl.iidm.network.ThreeWindingsTransformer;

/**
 * An action changing the tap position of a phase-shifting transformer.
 *
 * @author Hadrien Godard <hadrien.godard@artelys.com>
 */
public class PhaseTapChangerTapPositionAction extends AbstractTapChangerTapPositionAction {

    public static final String NAME = "PHASE_TAP_CHANGER_TAP_POSITION";

    public PhaseTapChangerTapPositionAction(String id, String transformerId, boolean relativeValue, int tapPosition) {
        super(id, transformerId, relativeValue, tapPosition, null);
    }

    public PhaseTapChangerTapPositionAction(String id, String transformerId, boolean relativeValue, int tapPosition, ThreeWindingsTransformer.Side side) {
        super(id, transformerId, relativeValue, tapPosition, side);
    }

    @Override
    public String getType() {
        return NAME;
    }
}
