/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Anis Touri <anis-1.touri@rte-france.com>
 */

public class RemoveHvdcLineBuilder {
    private String hvdcLineId;
    private List<String> shuntCompensatorIds = Collections.emptyList();

    public RemoveHvdcLine build() {
        Objects.requireNonNull(hvdcLineId);
        return new RemoveHvdcLine(hvdcLineId, shuntCompensatorIds);
    }

    /**
     * @param hvdcLineId the non-null ID of the HVDC line
     */
    public RemoveHvdcLineBuilder withHvdcLineId(String hvdcLineId) {
        this.hvdcLineId = hvdcLineId;
        return this;
    }

    /**
     * @param shuntCompensatorIds the IDs of the shunt compensator
     */
    public RemoveHvdcLineBuilder withShuntCompensatorIds(List<String> shuntCompensatorIds) {
        Objects.requireNonNull(shuntCompensatorIds);
        this.shuntCompensatorIds = shuntCompensatorIds;
        return this;
    }
}
