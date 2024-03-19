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
 * An action modifying the regulation of a two or three windings transformer
 *
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public abstract class AbstractTapChangerRegulationAction extends AbstractTapChangerAction {

    private final boolean regulating;

    protected AbstractTapChangerRegulationAction(String id, String transformerId, ThreeSides side, boolean regulating) {
        super(id, transformerId, side);
        this.regulating = regulating;
    }

    public boolean isRegulating() {
        return regulating;
    }
}
