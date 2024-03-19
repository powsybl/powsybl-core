/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public class ShuntCompensatorPositionActionBuilder {

    private String id;
    private String shuntCompensatorId;
    private Integer sectionCount = null;

    public ShuntCompensatorPositionAction build() {
        if (sectionCount == null) {
            throw new IllegalArgumentException("sectionCount in undefined");
        }
        if (sectionCount < 0) {
            throw new IllegalArgumentException("sectionCount should be positive for a shunt compensator");
        }
        return new ShuntCompensatorPositionAction(id, shuntCompensatorId, sectionCount);
    }

    public ShuntCompensatorPositionActionBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public ShuntCompensatorPositionActionBuilder withShuntCompensatorId(String shuntCompensatorId) {
        this.shuntCompensatorId = shuntCompensatorId;
        return this;
    }

    public ShuntCompensatorPositionActionBuilder withSectionCount(int sectionCount) {
        this.sectionCount = sectionCount;
        return this;
    }
}
