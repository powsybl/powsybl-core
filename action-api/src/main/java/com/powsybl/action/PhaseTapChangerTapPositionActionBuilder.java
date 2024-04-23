/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

/**
 *
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class PhaseTapChangerTapPositionActionBuilder extends AbstractTapChangerTapPositionActionBuilder<PhaseTapChangerTapPositionActionBuilder> {

    @Override
    public String getType() {
        return PhaseTapChangerTapPositionAction.NAME;
    }

    @Override
    public PhaseTapChangerTapPositionAction build() {
        return new PhaseTapChangerTapPositionAction(this.getId(), this.getTransformerId(), this.isRelativeValue(),
            this.getTapPosition(), this.getSide().orElse(null));
    }
}
