/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.reporter.Reporter;

import java.util.*;

/**
 * @author Anis Touri <anis-1.touri@rte-france.com>
 */

public class RemoveHvdcLineBuilder {

    private String hvdcLineId;

    private final List<String> shuntCompensatorIds = new ArrayList<>();

    private Reporter reporter = Reporter.NO_OP;

    public RemoveHvdcLine build() {
        Objects.requireNonNull(hvdcLineId);
        return new RemoveHvdcLine(hvdcLineId, shuntCompensatorIds, reporter);
    }

    /**
     * @param hvdcLineId the non-null ID of the Hvdc line
     */
    public RemoveHvdcLineBuilder withHvdcLineId(String hvdcLineId) {
        this.hvdcLineId = hvdcLineId;
        return this;
    }

    /**
     * If we remove an hvdc line with lcc converter stations, each converter stations can be associated, in the same
     * voltage level to a series of shunt compensators (also called filters).
     *
     * @param shuntCompensatorIds IDs of the shunt compensators that must be deleted.
     */
    public RemoveHvdcLineBuilder withShuntCompensatorIds(List<String> shuntCompensatorIds) {
        this.shuntCompensatorIds.clear();
        this.shuntCompensatorIds.addAll(Objects.requireNonNull(shuntCompensatorIds));
        return this;
    }

    public RemoveHvdcLineBuilder withShuntCompensatorIds(String... shuntCompensatorIds) {
        this.shuntCompensatorIds.clear();
        this.shuntCompensatorIds.addAll(Arrays.asList(shuntCompensatorIds));
        return this;
    }

    public RemoveHvdcLineBuilder withReporter(Reporter reporter) {
        this.reporter = reporter;
        return this;
    }
}
