/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import java.util.Objects;

/**
 *
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class RatioTapChangerRegulationActionBuilder extends AbstractTapChangerActionRegulationBuilder<RatioTapChangerRegulationActionBuilder> {
    private Double targetV;

    @Override
    public RatioTapChangerRegulationAction build() {
        return new RatioTapChangerRegulationAction(id, transformerId, side, regulating, targetV);
    }

    public RatioTapChangerRegulationActionBuilder withTargetV(Double targetV) {
        this.targetV = targetV;
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
        RatioTapChangerRegulationActionBuilder that = (RatioTapChangerRegulationActionBuilder) o;
        return Objects.equals(targetV, that.targetV);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetV);
    }

}
