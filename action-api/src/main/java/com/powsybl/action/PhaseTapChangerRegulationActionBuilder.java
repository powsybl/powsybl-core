/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.powsybl.iidm.network.PhaseTapChanger;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class PhaseTapChangerRegulationActionBuilder extends AbstractTapChangerRegulationActionBuilder<PhaseTapChangerRegulationActionBuilder> {

    private PhaseTapChanger.RegulationMode regulationMode;
    private Double regulationValue;

    @Override
    public String getType() {
        return PhaseTapChangerRegulationAction.NAME;
    }

    @Override
    public PhaseTapChangerRegulationAction build() {
        return new PhaseTapChangerRegulationAction(this.getId(), this.getTransformerId(), this.getSide().orElse(null),
            this.isRegulating(), regulationMode, regulationValue);
    }

    public PhaseTapChangerRegulationActionBuilder withRegulationMode(PhaseTapChanger.RegulationMode regulationMode) {
        this.regulationMode = regulationMode;
        return this;
    }

    public PhaseTapChangerRegulationActionBuilder withRegulationValue(Double regulationValue) {
        this.regulationValue = regulationValue;
        return this;
    }
}
