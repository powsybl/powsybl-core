/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import java.util.List;

/**
 * @author Anis Touri <anis-1.touri@rte-france.com>
 */

public class RemoveHvdcLineBuilder {
    private String hvdcLineId = null;
    private List<String> mscIds = null;

    public RemoveHvdcLine build() {
        return new RemoveHvdcLine(hvdcLineId, mscIds);
    }

    /**
     * @param hvdcLineId the non-null ID of the lcc Ccnverter station
     */
    public RemoveHvdcLineBuilder withHvdcLineId(String hvdcLineId, List<String> mscIds) {
        this.hvdcLineId = hvdcLineId;
        this.mscIds = mscIds;
        return this;
    }
}
