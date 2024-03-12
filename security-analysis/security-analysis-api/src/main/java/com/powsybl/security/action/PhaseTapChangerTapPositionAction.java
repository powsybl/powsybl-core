/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.action;

import com.powsybl.contingency.contingency.list.identifier.IdBasedNetworkElementIdentifier;
import com.powsybl.contingency.contingency.list.identifier.NetworkElementIdentifier;
import com.powsybl.iidm.network.ThreeSides;

import java.util.Collections;
import java.util.List;

/**
 * An action changing the tap position of a phase-shifting transformer.
 *
 * @author Hadrien Godard {@literal <hadrien.godard@artelys.com>}
 */
public class PhaseTapChangerTapPositionAction extends AbstractTapChangerTapPositionAction {

    public static final String NAME = "PHASE_TAP_CHANGER_TAP_POSITION";

    public PhaseTapChangerTapPositionAction(String id, String transformerId, boolean relativeValue, int tapPosition) {
        super(id, Collections.singletonList(new IdBasedNetworkElementIdentifier(transformerId)), relativeValue, tapPosition, null);
    }

    public PhaseTapChangerTapPositionAction(String id, List<NetworkElementIdentifier> tapChangerIdentifiers, boolean relativeValue, int tapPosition) {
        super(id, tapChangerIdentifiers, relativeValue, tapPosition, null);
    }

    public PhaseTapChangerTapPositionAction(String id, String transformerId, boolean relativeValue, int tapPosition, ThreeSides side) {
        super(id, Collections.singletonList(new IdBasedNetworkElementIdentifier(transformerId)), relativeValue, tapPosition, side);
    }

    public PhaseTapChangerTapPositionAction(String id, List<NetworkElementIdentifier> tapChangerIdentifiers, boolean relativeValue, int tapPosition, ThreeSides side) {
        super(id, tapChangerIdentifiers, relativeValue, tapPosition, side);
    }

    @Override
    public String getType() {
        return NAME;
    }
}
