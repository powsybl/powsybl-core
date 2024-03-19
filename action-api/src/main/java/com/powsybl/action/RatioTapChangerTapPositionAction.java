/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.powsybl.iidm.network.ThreeSides;

/**
 * An action modifying the tap position of a ratio transformer
 *
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public class RatioTapChangerTapPositionAction extends AbstractTapChangerTapPositionAction {
    public static final String NAME = "RATIO_TAP_CHANGER_TAP_POSITION";

    public RatioTapChangerTapPositionAction(String id, String transformerId, boolean relativeValue, int value, ThreeSides side) {
        super(id, transformerId, relativeValue, value, side);
    }

    public RatioTapChangerTapPositionAction(String id, String transformerId, boolean relativeValue, int value) {
        super(id, transformerId, relativeValue, value, null);
    }

    @Override
    public String getType() {
        return NAME;
    }
}
