/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.reporter.Reporter;

/**
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class RemoveVoltageLevelBuilder {
    private String voltageLevelId = null;
    private Reporter reporter = Reporter.NO_OP;

    public RemoveVoltageLevel build() {
        return new RemoveVoltageLevel(voltageLevelId, reporter);
    }

    /**
     * @param voltageLevelId the non-null ID of the voltage level
     */
    public RemoveVoltageLevelBuilder withVoltageLevelId(String voltageLevelId) {
        this.voltageLevelId = voltageLevelId;
        return this;
    }

    public RemoveVoltageLevelBuilder withReporter(Reporter reporter) {
        this.reporter = reporter;
        return this;
    }
}
