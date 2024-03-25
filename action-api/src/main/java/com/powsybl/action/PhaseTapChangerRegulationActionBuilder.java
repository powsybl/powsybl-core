/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.powsybl.iidm.network.PhaseTapChanger;

import java.util.Objects;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class PhaseTapChangerRegulationActionBuilder extends AbstractTapChangerActionRegulationBuilder<PhaseTapChangerRegulationActionBuilder> {

    private PhaseTapChanger.RegulationMode regulationMode;
    private Double regulationValue;

    @Override
    public PhaseTapChangerRegulationAction build() {
        return new PhaseTapChangerRegulationAction(id, transformerId, side, regulating, regulationMode, regulationValue);
    }

    public PhaseTapChangerRegulationActionBuilder withRegulationMode(PhaseTapChanger.RegulationMode regulationMode) {
        this.regulationMode = regulationMode;
        return this;
    }

    public PhaseTapChangerRegulationActionBuilder withRegulationValue(Double regulationValue) {
        this.regulationValue = regulationValue;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PhaseTapChangerRegulationActionBuilder that = (PhaseTapChangerRegulationActionBuilder) o;
        return regulationMode == that.regulationMode && Objects.equals(regulationValue, that.regulationValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(regulationMode, regulationValue);
    }
}
